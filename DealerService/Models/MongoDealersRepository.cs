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
        MongoServer _server;
        MongoDatabase _db;

        public MongoDealersRepository()
        {
            _client = new MongoClient("mongodb://holdevopsmongodb:8jVuAUdL5wDZutQ5r1FQR0zUEi276FpjdcZjOG6LIDX0DgKuTvOHForS0icrPjGmOv8Kfh60qNi0RWLDBKb80Q==@holdevopsmongodb.documents.azure.com:10250/?ssl=true");
            _server = _client.GetServer();
            _db = _server.GetDatabase("ordering");
        }

        public Dealer getDealer(String name)
        {
            var res = Query<Dealer>.EQ(p => p.name, name);
            var existing = _db.GetCollection<Dealer>("Dealer").FindOne(res);

            if (existing != null)
            {
                return existing;
            }
            return null;
        }

        public List<Dealer> getDealers()
        {
            List<Dealer> result = new List<Dealer>();

            var found = _db.GetCollection<Dealer>("Dealer").FindAll();

            foreach (Dealer dealer in found)
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
            var res = Query<Dealer>.EQ(pd => pd.name, dealer.name);
            var operation = Update<Dealer>.Replace(dealer);
            Dealer mongoDealer = new Dealer();
            var existing = _db.GetCollection<Dealer>("Dealer").FindOne(res);

            if (existing != null)
            {
                _db.GetCollection<Dealer>("Dealer").Update(res, operation);
            }
            else
            {
                _db.GetCollection<Dealer>("Dealer").Save(dealer);
            }
            
            return existing != null;
        }

        public Boolean removeDealer(String name, String eTag)
        {
            var res = Query<Dealer>.EQ(e => e.name, name);
            var operation = _db.GetCollection<Dealer>("Dealer").Remove(res);
            return true;
        }
        
    }
}
