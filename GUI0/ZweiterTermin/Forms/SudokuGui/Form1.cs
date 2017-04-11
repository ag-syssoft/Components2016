using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Security.Permissions;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Xml;
using Newtonsoft.Json;
using System.Net;
using System.Net.Sockets;

namespace SudokuGui
{
    public partial class Form1 : Form
    {
        static int size;
        int[] sudokuValues;
        FileSystemWatcher xmlWatcher;
        FileSystemWatcher jsonWatcher;
        [PermissionSet(SecurityAction.Demand, Name = "FullTrust")]
        public Form1()
        {
            Directory.CreateDirectory("./InMessages");
            Directory.CreateDirectory("./OutMessages");
            Directory.CreateDirectory("./GitOutMessages");
            Directory.CreateDirectory("./GitInMessages");
            xmlWatcher = new FileSystemWatcher("./InMessages");
            xmlWatcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
           | NotifyFilters.FileName | NotifyFilters.DirectoryName;
            xmlWatcher.Filter = "*.xml";

            xmlWatcher.Changed += new FileSystemEventHandler(OnChangedXml);
            xmlWatcher.Renamed += new RenamedEventHandler(OnChangedXml);
            xmlWatcher.Created += new FileSystemEventHandler(OnChangedXml);
            xmlWatcher.EnableRaisingEvents = true;

            jsonWatcher = new FileSystemWatcher("./InMessages");
            jsonWatcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
           | NotifyFilters.FileName | NotifyFilters.DirectoryName;
            jsonWatcher.Filter = "*.json";

            jsonWatcher.Changed += new FileSystemEventHandler(OnChangedJson);
            jsonWatcher.Renamed += new RenamedEventHandler(OnChangedJson);
            jsonWatcher.Created += new FileSystemEventHandler(OnChangedJson);
            jsonWatcher.EnableRaisingEvents = true;


            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            size = (int)numericUpDown1.Value;
            sudokuValues = new int[size * size * 9];
            buildSudoku();
            
        }

        private void buildSudoku()
        {
            flowLayoutPanel1.Controls.Clear();
            int halfWidth = this.Width / 2;
            int halfHeight = this.Height / 3;


            int x = halfWidth - halfWidth / 2;
            int y = halfHeight - halfHeight / 2;
            int x_inc = (x + halfWidth) / (size);
            int y_inc = (y + halfHeight) / (size);

            FlowLayoutPanel sudoku = new FlowLayoutPanel();       
            sudoku.AutoSize = true;


            int offset = 0;
            for (int i = 0; i < 9; ++i)
            {
                FlowLayoutPanel field = createSingleSudokuField(offset, out offset);
                sudoku.Controls.Add(field);
                
                if ((i + 1) % 3 == 0)
                {
                    sudoku.SetFlowBreak(field, true);
                    offset += (size-1) * size * 3; 
                    x = halfWidth - halfWidth / 2;
                    y += y_inc;
                }
                else
                {
                    x += x_inc;
                }
            }
            sudoku.Location = new Point(halfWidth - sudoku.Size.Width, halfHeight-sudoku.Size.Height);
            flowLayoutPanel1.Controls.Add(sudoku);
            this.Refresh();
        }
        private FlowLayoutPanel createSingleSudokuField(int offset, out int outOffset)
        {
            FlowLayoutPanel field = new FlowLayoutPanel();
            field.AutoSize = true;
            field.AutoSizeMode = AutoSizeMode.GrowAndShrink;
            field.BackColor = Color.White;
            field.BorderStyle = BorderStyle.FixedSingle;
            for (int i = 0; i < size*size; ++i) {
                int row = i / size;
                int index = i % size;
                Label number = new Label();
                number.Name = ""+ (index + offset + row * size * 3);
                number.Text = sudokuValues[index + offset + row*size*3] != 0 ?"" +sudokuValues[index + offset + row*size*3] : "?";
                number.Font = new Font(FontFamily.GenericSansSerif, 20.0f, FontStyle.Bold);
                number.AutoSize = true;
                number.Click += new EventHandler(numberClicked);
                number.DoubleClick += new EventHandler(numberClicked);

                field.Controls.Add(number);
                if ((i + 1) % size == 0)
                {                   
                    field.SetFlowBreak(number, true);
                }     
            }
            outOffset = offset + size;
            return field;
        }

        private void numberClicked(object sender, EventArgs e)
        {
            MouseEventArgs me = (MouseEventArgs)e;
            Label num = (Label)sender;
            int index = int.Parse(num.Name);
            int textVal = -1;
            if(int.TryParse(num.Text,out textVal))
            {
                if (me.Button == MouseButtons.Left)
                {
                    if (textVal < size*size)
                        textVal++;
                    else
                        textVal = 1;
                } else
                {
                    if (textVal > 1)
                        textVal--;
                    else
                        textVal = size*size;
                }
            } else
            {
                if (me.Button == MouseButtons.Left)
                {
                    textVal = 1;
                }else
                {
                    textVal = size*size;
                }
            }
            sudokuValues[index] = textVal;
            num.Text = "" + textVal;
        }
        //Register at Broker
        private void button1_Click(object sender, EventArgs e)
        {
            writeMessageToCamel(getSudokuString(), "register:gui");
        }

        //Generate Sudoku
        private void button2_Click(object sender, EventArgs e)
        {
            writeMessageToCamel(getSudokuString(), "generate:"+difficulty.Value);
        }

        //Solve Sudoku
        private void button3_Click(object sender, EventArgs e)
        {
            writeMessageToCamel(getSudokuString(), "solve");
        }

        //Unregister Broker
        private void button4_Click(object sender, EventArgs e)
        {
            writeMessageToCamel(getSudokuString(), "unregister");
            connected_picture.BackColor = Color.Red;
        }

        private string getSudokuString()
        {
            string str = "";
            for(int i = 0; i<sudokuValues.Length; ++i)
            {
                if (i != 0)
                    str += ",";
                str += sudokuValues[i];
            }
            return str;
        }

        private void writeMessageToCamel(string sudoku, string instruction)
        {
            string id = Guid.NewGuid().ToString();
            string sender = "netty4:tcp://"+GetLocalIPAddress()+":8888?textline=true";
            /*
            XmlDocument xml = new XmlDocument();
            XmlElement message = xml.CreateElement("message");

            XmlElement request_id = xml.CreateElement("request-id");
            request_id.InnerText = id;

            XmlElement send = xml.CreateElement("sender");
            send.InnerText = sender;

            XmlElement sudoku_elem = xml.CreateElement("sudoku");
            sudoku_elem.InnerText = sudoku;

            XmlElement instruction_elem = xml.CreateElement("instruction");
            instruction_elem.InnerText = instruction;

            message.AppendChild(request_id);
            message.AppendChild(send);
            message.AppendChild(sudoku_elem);
            message.AppendChild(instruction_elem);
            xml.AppendChild(message);
            XmlWriter writer = XmlWriter.Create("./OutMessages/out.xml");
            xml.WriteContentTo(writer);
            writer.Close();
            */
            JsonMessage msg = new JsonMessage(sender, instruction, sudokuValues, id);
            File.WriteAllText("./OutMessages/out.json", JsonConvert.SerializeObject(msg));

        }

        private void decodeXml(XmlDocument doc)
        {
            
            string instruction = doc.GetElementsByTagName("instruction").Item(0).InnerText;
            if (instruction.Equals("display") || instruction.Contains("solved"))
            {
                string sudoku = doc.GetElementsByTagName("sudoku").Item(0).InnerText;
                char[] delimiters = new char[2];
                delimiters[0] = ',';
                delimiters[1] = ' ';
                string[] explode = sudoku.Split(delimiters);
                sudokuValues = new int[size * size * 9];
                for (int i = 0; i < explode.Length; i++)
                {
                    sudokuValues[i] = int.Parse(explode[i]);              
                }
                buildSudoku();
            }
        }

        private void OnChangedXml(object source, FileSystemEventArgs e)
        {
            MethodInvoker mi = delegate () { OnFileChanged(e.FullPath, FileType.XML); };
            this.Invoke(mi);
        }

        private void OnChangedJson(object source, FileSystemEventArgs e)
        {
            MethodInvoker mi = delegate () { OnFileChanged(e.FullPath, FileType.JSON); };
            this.Invoke(mi);
        }

        enum FileType
        {
            XML,JSON
        }

        private void OnFileChanged(string path, FileType type)
        {
            try
            {
                if (type == FileType.XML)
                {
                    XmlDocument doc = new XmlDocument();
                    doc.Load(path);
                    decodeXml(doc);
                    File.Delete(path);
                }

                if(type == FileType.JSON)
                {
                    using(StreamReader r = new StreamReader(path))
                    {
                        string json = r.ReadToEnd();
                        if (json.Contains("register"))
                        {
                            connected_picture.BackColor = Color.Green;
                            return;
                        }

                        Newtonsoft.Json.Linq.JObject obj = (Newtonsoft.Json.Linq.JObject)JsonConvert.DeserializeObject(json);
                        JsonMessage msg = obj.ToObject<JsonMessage>();
                        if (msg.instruction.Equals("display") || msg.instruction.Contains("solved")) {
                            sudokuValues = msg.sudoku;
                            buildSudoku();
                            File.Delete(path);
                            
                        }
                        
                        if (msg.instruction.Equals("commit"))
                        {
                            int num = int.Parse(commits_number.Text);
                            commits_number.Text = "" + ++num;
                        }
                    }
                }

            }
            catch (Exception ex)
            {

            }
        }

        private void numericUpDown1_ValueChanged(object sender, EventArgs e)
        {

            size = (int)numericUpDown1.Value;
            sudokuValues = new int[size * size * 9];
            buildSudoku();
        }

        //Push
        private void button5_Click(object sender, EventArgs e)
        {
            string id = Guid.NewGuid().ToString();
            string msg_sender = "git://../sudoku_communicate?remoteName=origin&remotePath=https://github.com/sudokuGit/sudoku_communicate.git";
            string instruction = "display";
            JsonMessage push_sudoku = new JsonMessage(msg_sender, instruction, sudokuValues, id);
            Directory.CreateDirectory("../../../../sudoku_communicate/" + branch.Text);
            File.WriteAllText("../../../../sudoku_communicate/"+ branch.Text+"/sudoku.json", JsonConvert.SerializeObject(push_sudoku));

            GitMessage msg = new GitMessage(branch.Text,"push");
            string push_msg = JsonConvert.SerializeObject(msg);
            File.WriteAllText("./GitOutMessages/out.json", push_msg);
        }

        //Pull
        private void button6_Click(object sender, EventArgs e)
        {
            GitMessage msg = new GitMessage(branch.Text, "pull");
            File.WriteAllText("./GitOutMessages/out.json", JsonConvert.SerializeObject(msg));

            readPull();
        }

        public void readPull()
        {
            System.Threading.Thread.Sleep(2500);
            using (StreamReader r = new StreamReader("./GitInMessages/" + branch.Text + "/sudoku.json"))
            {
                string json = r.ReadToEnd();
                Newtonsoft.Json.Linq.JObject obj = (Newtonsoft.Json.Linq.JObject)JsonConvert.DeserializeObject(json);
                JsonMessage pull = obj.ToObject<JsonMessage>();
                int[] pulled_sudoku = pull.sudoku;
                sudokuValues = new int[size * size * 9];
                for (int i = 0; i < Math.Min(pulled_sudoku.Length, sudokuValues.Length); ++i)
                {
                    sudokuValues[i] = pulled_sudoku[i];
                }
                commits_number.Text = "" + 0;
                buildSudoku();
            }
        }
        public static string GetLocalIPAddress()
        {
            var host = Dns.GetHostEntry(Dns.GetHostName());
            foreach (var ip in host.AddressList)
            {
                if (ip.AddressFamily == AddressFamily.InterNetwork)
                {
                    return ip.ToString();
                }
            }
            throw new Exception("Local IP Address Not Found!");
        }
    }
}
