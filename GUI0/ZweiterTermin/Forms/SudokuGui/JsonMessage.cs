using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SudokuGui
{
    class JsonMessage
    {
        public string reguest_id { get; set; }
        public string instruction{ get; set; }
        public int[] sudoku{ get; set; }
        public string sender { get; set; }


        public JsonMessage()
        {
            sender = "";
            instruction = "";
            sudoku = new int[3 * 3 * 9];
            reguest_id = "";
        }

        public JsonMessage(string newSender, string newInstruction, int[] newSudoku, string newRequest_id)
        {
            setMessage(newSender, newInstruction, newSudoku, newRequest_id);
        }

        public void setMessage(string newSender, string newInstruction, int[] newSudoku, string newRequest_id)
        {
            this.sender = newSender;
            this.instruction = newInstruction;
            this.sudoku = newSudoku;
            this.reguest_id = newRequest_id;
        }
    }
}
