using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.AspNetCore.Mvc;
using DealerApi.Models;
using Microsoft.Extensions.Logging;
using MongoDB.Bson;
using Microsoft.Extensions.Configuration;

namespace DealerApi.Controllers
{
    [Route("api/Dealer")]
    public class DealerController : Controller
    {
        readonly ILogger<DealerController> _log;
        MongoDealersRepository objds;

        public DealerController(ILogger<DealerController> log,IConfiguration iconfiguration)
        {
            _log = log;
            log.LogInformation("--------------------------------------------------");
            log.LogInformation("---debug----Creating instance Dealer");
            log.LogInformation(Environment.GetEnvironmentVariable("mongo_connection"));
            log.LogInformation(Environment.GetEnvironmentVariable("mongo_database"));
            log.LogInformation("--------------------------------------------------");
            objds = new MongoDealersRepository(Environment.GetEnvironmentVariable("mongo_connection"));
        }

        /// <summary>
        /// Function to get all the dealers with the details 
        /// </summary>
        /// <returns>Returns all the dealers</returns>
        [HttpGet]
        public IActionResult getDealers()
        {
            try
            {
                List<Dealer> dealers = objds.getDealers();

                if (dealers == null || dealers.Count() == 0)
                {
                    return new NotFoundResult();
                }
                else
                {
                    return new OkObjectResult(dealers);
                }

            }
            catch (Exception exc)
            {
                _log.LogInformation(exc.Message);
                return new BadRequestObjectResult(exc.Message);
            }
        }

        /// <summary>
        /// Function to get the dealer with the name
        /// </summary>
        /// <param name="name">name of the dealer as input parameter</param>
        /// <returns>Returns the detail of the dealer as name provided in parameter</returns>
        [HttpGet("{name}")]
        public IActionResult getDealer(String name)
        {
            try
            {
                Dealer dealers = objds.getDealer(name);

                if (dealers == null)
                {
                    return new NotFoundResult();
                }
                else
                {
                    return new OkObjectResult(dealers);
                }

            }
            catch (Exception exc)
            {
                _log.LogInformation(exc.Message);
                return new BadRequestObjectResult(exc.Message);
            }
        }

        /// <summary>
        /// Function to add dealer
        /// </summary>
        /// <param name="info">Info is the object of the Dealer Class</param>
        /// <returns>Returns the Ok result or Dealer already exist result</returns>
        [HttpPost]
        public IActionResult addDealer([FromBody]Dealer info)
        {
            String errorMsg = info.validate();
            if (errorMsg != null)
            {
                return new BadRequestResult();
            }

            try
            {
                Dealer dealer = objds.getDealer(info.getName());
                if (dealer != null)
                {
                    return new BadRequestObjectResult("Dealer already exists");
                }
                else
                {
                    objds.upsertDealer(info, null);
                    return new OkResult();
                }

            }
            catch (Exception exc)
            {
                _log.LogInformation(exc.Message);
                return new BadRequestObjectResult(exc.Message);
            }
        }

        /// <summary>
        /// Function to update Dealer
        /// </summary>
        /// <param name="name">find the dealer with name</param>
        /// <param name="info">update the dealer with new Info object details</param>
        /// <returns>Ok result in case of success</returns>
        [HttpPut("{name}")]
        public IActionResult updateDealer(String name, [FromBody]Dealer info)
        {
            String errorMsg = info.validate();
            if (errorMsg != null)
            {
                return new BadRequestObjectResult(errorMsg);
            }


            try
            {
                Dealer dealer = objds.getDealer(info.getName());
                if (dealer == null)
                {
                    return new NotFoundResult();
                }

                info.Id = new ObjectId(info.timestamp, info.machine, info.pid, info.increment);
                objds.upsertDealer(info, null);

                return new OkResult();
            }
            catch (Exception exc)
            {
                _log.LogInformation(exc.Message);
                return new BadRequestObjectResult(exc.Message);
            }
        }

        /// <summary>
        /// Function to remove Dealer
        /// </summary>
        /// <param name="name">input parameter as name which dealer needs to be removed</param>
        /// <returns>if removed No Content result will be returned else not found result</returns>
        [HttpDelete("{name}")]
        public IActionResult removeDealer(String name)
        {
            try
            {
                if (objds.removeDealer(name, null))
                {
                    return new NoContentResult();
                }
                else
                {
                    return new NotFoundResult();
                }
            }
            catch (Exception exc)
            {
                _log.LogInformation(exc.Message);
                return new BadRequestObjectResult(exc.Message);
            }

        }


    }
}
