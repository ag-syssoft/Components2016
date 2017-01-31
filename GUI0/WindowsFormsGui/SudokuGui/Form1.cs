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

namespace SudokuGui
{
    public partial class Form1 : Form
    {
        static int size;
        int[] sudokuValues;
        FileSystemWatcher watcher;
        [PermissionSet(SecurityAction.Demand, Name = "FullTrust")]
        public Form1()
        {
            Directory.CreateDirectory("./InMessages");
            Directory.CreateDirectory("./OutMessages");
            watcher = new FileSystemWatcher("./InMessages");
            watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
           | NotifyFilters.FileName | NotifyFilters.DirectoryName;
            watcher.Filter = "*.xml";

            watcher.Changed += new FileSystemEventHandler(OnChanged);
            watcher.Renamed += new RenamedEventHandler(OnChanged);
            watcher.Created += new FileSystemEventHandler(OnChanged);
            watcher.EnableRaisingEvents = true;
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
            

            for (int i = 0; i < size * size; ++i)
            {
                FlowLayoutPanel field = createSingleSudokuField(i);
                sudoku.Controls.Add(field);
                
                if ((i + 1) % size == 0)
                {
                    sudoku.SetFlowBreak(field, true);
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
        private FlowLayoutPanel createSingleSudokuField(int fieldNumber)
        {
            FlowLayoutPanel field = new FlowLayoutPanel();
            field.AutoSize = true;
            field.AutoSizeMode = AutoSizeMode.GrowAndShrink;
            field.BackColor = Color.White;
            field.BorderStyle = BorderStyle.FixedSingle;
            for (int i = 0; i < 9; ++i) {
                Label number = new Label();
                number.Name = ""+i;
                number.Text = sudokuValues[i + fieldNumber * 9] != 0 ?"" +sudokuValues[i + fieldNumber*9] : "?";
                number.Font = new Font(FontFamily.GenericSansSerif, 20.0f, FontStyle.Bold);
                number.AutoSize = true;
                number.Click += new EventHandler(numberClicked);
                number.DoubleClick += new EventHandler(numberClicked);

                field.Controls.Add(number);
                if ((i + 1) % 3 == 0)
                {                   
                    field.SetFlowBreak(number, true);
                }
                
                
            }
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
                    if (textVal < 9)
                        textVal++;
                    else
                        textVal = 1;
                } else
                {
                    if (textVal > 1)
                        textVal--;
                    else
                        textVal = 9;
                }
            } else
            {
                if (me.Button == MouseButtons.Left)
                {
                    textVal = 1;
                }else
                {
                    textVal = 9;
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
            writeMessageToCamel(getSudokuString(), "generate:"+size);
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
            XmlDocument xml = new XmlDocument();
            XmlElement message = xml.CreateElement("message");

            XmlElement request_id = xml.CreateElement("request_id");
            string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            string id = "";
            Random rand = new Random();
            for(int i = 0; i < 8; i++)
            {
                id += chars[rand.Next(chars.Length)];
            }
            request_id.InnerText = "GUI0_"+id;

            XmlElement send = xml.CreateElement("sender");
            send.InnerText = "gui0";

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

        private void OnChanged(object source, FileSystemEventArgs e)
        {
            MethodInvoker mi = delegate () { OnFileChanged(e.FullPath); };
            this.Invoke(mi);
        }

        private void OnFileChanged(string path)
        {
            try
            {
                XmlDocument doc = new XmlDocument();
                doc.Load(path);           
                decodeXml(doc);
                File.Delete(path);

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

        
    }
}
