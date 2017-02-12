using System;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using Newtonsoft.Json;
using System.Timers;

namespace Broker
{
    public class Broker
    {
        bool stupid = false;
        const string INQUEUE = "in";
        const string OUTEXCHANGE = "outExchange";
        const string CONFIG = "config";
	    const string SELFURI = "amqp://kompo:kompo@136.199.51.111:5672";
        const string MY = "rabbitmq://136.199.51.111/inExchange?username=kompo&password=kompo&skipQueueDeclare=true";
	    ConnectionFactory rqFactory;
        IModel rqModel;
        IBasicProperties rqPPersistent;
        List<Component> registered;
        List<Message> queue;
		Dictionary<string, Message> loesungen;
		Dictionary<Guid, string> currentRequest;
		Dictionary<string, DateTime> lastSeen;
		MD5 md5 = MD5.Create();
        public Broker()
        {
			loesungen = new Dictionary<String, Message>();
			currentRequest = new Dictionary<Guid, String>();
            registered = new List<Component>();
            queue = new List<Message>();
			lastSeen = new Dictionary<string, DateTime>();
			Timer t = new Timer(300000);
			t.Elapsed += cleanup;
			t.Start();
            rqFactory = new ConnectionFactory();
            rqFactory.Uri = SELFURI;
            IConnection conn = rqFactory.CreateConnection();
            rqModel = conn.CreateModel();
            rqPPersistent = rqModel.CreateBasicProperties();
            rqPPersistent.DeliveryMode = 2;
            EventingBasicConsumer consumer = new EventingBasicConsumer(rqModel);
            consumer.Received += onRecieve;
            rqModel.BasicConsume(INQUEUE, false, consumer);
            Console.Out.WriteLine("Broker up and running");
        }

        public Broker(bool stupid) : this()
        {
            Console.Out.WriteLine("Starting in stupid mode");
            this.stupid = stupid;
            
        }

        /// <summary>
        /// Registers a new component and sends the command to add a new queue to the camel instance
        /// </summary>
        /// <param name="c">Component to add</param>
        /// <returns>True on sucess</returns>
        public bool register(string instruction, string uri)
        {
			
            string type;

            try { type = instruction.Split(' ')[1]; }
            catch
            {
                type = instruction.Split(':')[1];
            }
            Component.Type t = Component.Type.GUI;
            switch (type)
            {
                case "broker":
                    t = Component.Type.Broker;
                    break;
                case "gui":
                    t = Component.Type.GUI;
                    break;
                case "solver":
                    t = Component.Type.Solver;
                    break;
                case "generator":
                    t = Component.Type.Generator;
                    break;
            }
			Component oldc = registered.Find(x => x.uri.Equals(uri));
			if (oldc != null) { unregister(uri);}
            Component c = new Component(t, uri);
            configureRoute(c);
            registered.Add(c);
            Console.Out.WriteLine("Broker:\t New Component (" + c.type + ") registerd");
            //try to flush queue
            flushQueue(c);
            return true;
        }

		public string getHash(int[] arr)
		{
			byte[] b = Encoding.Unicode.GetBytes(JsonConvert.SerializeObject(arr));
			byte[] hash = md5.ComputeHash(b);
			StringBuilder sBuilder = new StringBuilder();
			for (int i = 0; i < hash.Length; i++)
			{
				sBuilder.Append(hash[i].ToString("x2"));
			}
			return sBuilder.ToString();
		}
        /// <summary>
        /// Sends a component to the camel instance. If the camel instance does not know the component, a new route is added, otherwise deleted
        /// Every component gets an id that is used as a routing key
        /// </summary>
        /// <param name="c">Component to add</param>
        public void configureRoute(Component c)
        {
            //Sends a new route configuration to camel instance
            byte[] body = System.Text.Encoding.Default.GetBytes(c.uri);
            rqModel.BasicPublish(CONFIG, c.id.ToString(), false, rqPPersistent, body);
            Console.Out.WriteLine("Config:\t Send route to " + c.uri);
        }

        private void flushQueue(Component c)
        {
            Message[] messages = new Message[queue.Count];
            queue.CopyTo(messages);
            foreach (Message m in messages)
            {
                queue.Remove(m);
                switch (m.instruction)
                {
                    case Message.Instruction.SOLVE:
                        processGenerated(m);
                        break;
                    case Message.Instruction.SOLVED:
                        processSolved(m);
                        break;
                }
            }
        }

        public bool unregister(String uri)
        {
            Component c = registered.Find(x => x.uri.Equals(uri));
            if (c == null) return false;
            configureRoute(c);
            return registered.Remove(c);
        }

        public void processGenerated(Message m)
        {
			string hash = getHash(m.sudoku);
			if (loesungen.ContainsKey(hash))
			{
				Message rep;
				loesungen.TryGetValue(hash, out rep);
				rep.request_id = m.request_id;
				rep.sender = MY;
				sendMessage(rep, Component.Type.Generator);
				return;
			}
			try { registered.Find(x => x.uri == m.sender).busy = false; }
			catch (NullReferenceException e) { }
            m.sender = MY;
			currentRequest.Add(m.request_id,getHash(m.sudoku));
            Component c = sendMessage(m, Component.Type.Solver);
            //Mark solver as busy
            //if no fitting component is currently registered, store it in queue to send it later
            if (c != null) c.busy = true;
            else queue.Add(m);


        }

        public void processSolved(Message m)
        {
			//lookup in current requests
			if (currentRequest.ContainsKey(m.request_id))
			{
				string hash;
				currentRequest.TryGetValue(m.request_id, out hash);
				loesungen.Add(hash, m);
				currentRequest.Remove(m.request_id);
			}
            registered.Find(x => x.uri == m.sender).busy = false;
            m.sender = MY;
            Component c = sendMessage(m, Component.Type.Generator);
            //Mark generator as busy
            //if no fitting component is currently registered, store it in queue to send it later
            if (c != null) c.busy = true;
            else queue.Add(m);
        }

        public void processPing( Message m)
        {
            m.instruction = "pong";
            Component c = registered.Find(x => (x.uri == m.sender));
            if (c == null) return;
            sendMessage(m,c);
        }

        public Component sendMessage(Message m, Component.Type recipientType)
        {
            String json = JsonConvert.SerializeObject(m);
            json = json.Replace("request_id", "request-id");
            byte[] body = System.Text.Encoding.Default.GetBytes(json);
            if (recipientType == Component.Type.ALL)
            {
                foreach(Component c in registered)
                {
                    rqModel.BasicPublish(OUTEXCHANGE, c.id.ToString(), false, rqPPersistent, body);
                }
                Console.Out.WriteLine("Send:\t To all:\t" + json);
                return null;
            }
            else if(recipientType == Component.Type.ALLEXCEPTBROKER)
            {
                foreach (Component c in registered)
                {
                    if (c.type != Component.Type.Broker)
                    {
                        rqModel.BasicPublish(OUTEXCHANGE, c.id.ToString(), false, rqPPersistent, body);
                    }
                }
                Console.Out.WriteLine("Send:\t To all except broker:\t" + json);
                return null;
            }
            //try to find idling component
            else
            {
                Component c = registered.Find(x => (x.type == recipientType && !x.busy));
                //try to find a fitting (but busy) component
                if (c == null) c = registered.Find(x => x.type == recipientType);
                if (c != null)
                {
                    json = JsonConvert.SerializeObject(m);
                    body = System.Text.Encoding.Default.GetBytes(json);
                    rqModel.BasicPublish(OUTEXCHANGE, c.id.ToString(), false, rqPPersistent, body);
                    Console.Out.WriteLine("Send:\t To " + recipientType.ToString() + ":\t" + json);
                }
                return c;
            }
       
        }

        public void sendMessage(Message m,Component c)
        {
            m.sender = MY;
            String json = JsonConvert.SerializeObject(m);
            json = json.Replace("request_id", "request-id");
            byte[] body = System.Text.Encoding.Default.GetBytes(json);
            rqModel.BasicPublish(OUTEXCHANGE, c.id.ToString(), false, rqPPersistent, body);
        }

        public void onRecieve(object sender, BasicDeliverEventArgs e)
        {
			String message = System.Text.Encoding.Default.GetString(e.Body);
			message = message.Replace("request-id", "request_id");
			Message m;
			try
			{
				m = JsonConvert.DeserializeObject<Message>(message);
			}
			catch (Exception ex){
				rqModel.BasicAck(e.DeliveryTag,false);
				Console.WriteLine("Received wrong message");
				return;
			}
			Console.Out.WriteLine("Received:\t " + m.instruction);
			lastSeen[m.sender] = DateTime.Now;
			string instruction = m.instruction.Split(':')[0];
			switch (instruction)
			{
				case Message.Instruction.REGISTER:
					register(m.instruction, m.sender);
					break;
				case Message.Instruction.UNREGISTER:
					unregister(m.sender);
					break;
				case Message.Instruction.SOLVE:
					processGenerated(m);
					break;
				case Message.Instruction.SOLVED:
					processSolved(m);
					break;
                case Message.Instruction.PING:
                    processPing(m);
                    break;
			}
			if (stupid)
			{
				try
				{
					//Doesnt work this way, broker is never the sender
					//Todo: change to identify already sent messages (looping)
					if (registered.Find(x => x.uri == m.sender).type == Component.Type.Broker)
					{
						sendMessage(m, Component.Type.ALLEXCEPTBROKER);
					}
					else
					{
						sendMessage(m, Component.Type.ALL);
					}
				}
				catch (NullReferenceException err)
				{

				}
			}
			rqModel.BasicAck(e.DeliveryTag, false);
        }

		public void cleanup(object sender,ElapsedEventArgs e)
		{
			foreach (KeyValuePair<string, DateTime> kp in lastSeen)
			{
				if (kp.Value.AddMinutes(5) < DateTime.Now)
				{
					Console.Out.WriteLine("Unregistering " + kp.Key + " due to inactivity");
					unregister(kp.Key);
                    lastSeen.Remove(kp.Key);
				}
			}
		}

    }
}
