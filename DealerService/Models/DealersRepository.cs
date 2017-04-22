using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DealerApi.Models
{
    public interface DealersRepository
    {
        List<Dealer> getDealers();

        Dealer getDealer(String name);

        Boolean upsertDealer(Dealer dealer, String eTag);

        Boolean removeDealer(String name, String eTag);
    }
}
