using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SudokuGui
{
    class GitMessage
    {
        public String branch;
        public String operation;

        public GitMessage()
        {
            branch = "master";
            operation = "pull";
        }

        public GitMessage(String branch, String operation)
        {
            this.branch = branch;
            this.operation = operation;
        }

        public String getBranch()
        {
            return branch;
        }

        public void setBranch(String branch)
        {
            this.branch = branch;
        }

        public String getOperation()
        {
            return branch;
        }

        public void setOperation(String operation)
        {
            this.operation = operation;
        }
    }
}
