﻿namespace SocialToilet.Api.Database
{
    using System.Data.Entity;
    using System.Data.Entity.Spatial;

    using SocialToilet.Api.Models;

    public class SocialToiletInitializer : DropCreateDatabaseAlways<SocialToiletContext>
    {
        protected override void Seed(SocialToiletContext context)
        {
            // close to Rivadavia Park
            var formosa = new Toilet
                {
                    Address = "Formosa 252",
                    Description = "Zona residencial centrica",
                    Location = DbGeography.FromText("POINT(-58.43185901641846 -34.617934476251946)")
                };

            var viel = new Toilet
                           {
                               Address = "Viel 350",
                               Description = "Parrilla La silla electrica",
                               Location = DbGeography.FromText("POINT(-58.43277096748352 -34.61893219106914)")
                           };

            var fiuba = new Toilet
                            {
                                Address = "Paseo Colon 850",
                                Description = "De la facultad. No recomendado.",
                                Location = DbGeography.FromText("POINT(-58.3673894405365 -34.618128722554786)")
                            };

            context.Toilets.Add(formosa);
            context.Toilets.Add(viel);
            context.Toilets.Add(fiuba);

            var sebastian = new User { Name = "srodriguez", Password = "password" };
            var damian = new User { Name = "dschenkelman", Password = "password" };
            var matias = new User { Name = "mservetto", Password = "password" };

            context.Users.Add(sebastian);
            context.Users.Add(damian);
            context.Users.Add(matias);

            context.SaveChanges();

            context.Ratings.Add(new Rating() { ToiletId = formosa.Id, UserName = damian.Name, Value = 5 });
            context.Ratings.Add(new Rating() { ToiletId = viel.Id, UserName = sebastian.Name, Value = 2.5 });
            context.Ratings.Add(new Rating() { ToiletId = fiuba.Id, UserName = matias.Name, Value = 1 });

            context.SaveChanges();
        }
    }
}