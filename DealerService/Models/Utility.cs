using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Text;

namespace DealerApi.Models
{
    public class Utility
    {
        public static int validateStringField(String field, String fieldName, int count, StringBuilder errors)
        {
            if (isNullOrEmpty(field))
            {
                if (count == 0)
                {
                    errors.Append(String.Format("\"Empty %s field\"", fieldName));
                }
                else
                {
                    errors.Append(String.Format(",\"Empty %s field\"", fieldName));
                }
                count += 1;
            }
            return count;
        }

        public static Boolean isNullOrEmpty(String str)
        {
            return str == null || String.IsNullOrEmpty(str);
        }


    }
}
