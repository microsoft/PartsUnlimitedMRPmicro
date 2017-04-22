using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using Newtonsoft.Json;
using System;
using System.Text;

namespace DealerApi.Models
{
    public class Dealer
    {
        [JsonIgnore]
        public ObjectId Id { get; set; }

        [BsonElement("timestamp")]
        public int timestamp { get; set; }

        [BsonElement("machine")]
        public int machine { get; set; }

        [BsonElement("pid")]
        public short pid { get; set; }

        [BsonElement("increment")]
        public int increment { get; set; }

        [BsonElement("creationTime")]
        public DateTime creationTime { get; set; }

        [BsonElement("dealerid")]
        public double dealerid;

        [BsonElement("name")]
        public String name { get; set; }

        [BsonElement("contact")]
        public String contact { get; set; }

        [BsonElement("address")]
        public String address { get; set; }

        [BsonElement("email")]
        public String email { get; set; }

        [BsonElement("phone")]
        public String phone { get; set; }
        
        
        public String validate()
        {
            int count = 0;
            StringBuilder errors = new StringBuilder("{\"errors\": [");
            count = Utility.validateStringField(name, "name", count, errors);
            errors.Append("]}");

            return (count > 0) ? errors.ToString() : null;
        }
        
        public String getName()
        {
            return name;
        }
       
    }
}
