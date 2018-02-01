using MongoDB.Driver;
using MongoDB.Driver.Builders;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DealerApi.Models
{
    public class MongoDealersRepository : DealersRepository
    {

        MongoClient _client;
        IMongoDatabase _db;

        public MongoDealersRepository(string connectionstring)
        {
            _client = new MongoClient(connectionstring);
            _db = _client.GetDatabase(Environment.GetEnvironmentVariable("mongo_database"));
        }

        public Dealer getDealer(String name)
        {
            var res = Query<Dealer>.EQ(p => p.name, name);
            //var existing = _db.GetCollection<Dealer>("Dealer").FindOne(res);
            var existing = _db.GetCollection<Dealer>("Dealer").FindAsync(p => p.name == name).Result.First();

            if (existing != null)
            {
                return existing;
            }
            return null;
        }

        public List<Dealer> getDealers()
        {
            List<Dealer> result = new List<Dealer>();

            var found = _db.GetCollection<Dealer>("Dealer").Find(_ => true).ToListAsync();

            foreach (Dealer dealer in found.Result)
            {
                dealer.timestamp = dealer.Id.Timestamp;
                dealer.machine = dealer.Id.Machine;
                dealer.pid = dealer.Id.Pid;
                dealer.increment = dealer.Id.Increment;
                dealer.creationTime = dealer.Id.CreationTime;
                result.Add(dealer);
            }
            return result;
        }

        public bool upsertDealer(Dealer dealer, string eTag)
        {
            try
            {
                var res = Query<Dealer>.EQ(pd => pd.name, dealer.name);
                var operation = Update<Dealer>.Replace(dealer);
                Dealer mongoDealer = new Dealer();
                var existing = _db.GetCollection<Dealer>("Dealer").FindAsync(p => p.name == dealer.name).Result.First();

                if (existing != null)
                {
                    _db.GetCollection<Dealer>("Dealer").ReplaceOne(p => p.name == dealer.name, dealer);

                }
                else
                {
                    _db.GetCollection<Dealer>("Dealer").InsertOne(dealer);
                }

                return existing != null;
            }
            catch (Exception e)
            {
                if (e.Message.Contains("Sequence contains no elements"))
                {
                    _db.GetCollection<Dealer>("Dealer").InsertOne(dealer);

                }
                return true;
            }
        }

        public Boolean removeDealer(String name, String eTag)
        {
            var res = Query<Dealer>.EQ(e => e.name, name);
            var operation = _db.GetCollection<Dealer>("Dealer").FindOneAndDelete(p => p.name == name);
            return true;
        }

    }
}
