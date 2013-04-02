package org.objectquery.orientdbobjectquery;

import org.objectquery.orientdbobjectquery.domain.Dog;
import org.objectquery.orientdbobjectquery.domain.Home;
import org.objectquery.orientdbobjectquery.domain.Home.HomeType;
import org.objectquery.orientdbobjectquery.domain.Person;

import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class OrientDBTestHelper {

	private static OObjectDatabasePool pool;

	public static OObjectDatabasePool getPool() {
		if (pool == null) {
			new OObjectDatabaseTx("memory:test").create();
			pool = new OObjectDatabasePool("memory:test", "admin", "admin");
			initData();
		}
		return pool;
	}

	private static void initData() {
		OObjectDatabaseTx db;
		db = pool.acquire();
		db.getEntityManager().registerEntityClass(Home.class);
		db.getEntityManager().registerEntityClass(HomeType.class);
		db.getEntityManager().registerEntityClass(Dog.class);
		db.getEntityManager().registerEntityClass(Person.class);
		db.begin();

		Home tomHome = new Home();
		tomHome.setAddress("homeless");
		tomHome.setType(HomeType.HOUSE);
		tomHome = db.save(tomHome);

		Person tom = new Person();
		tom.setName("tom");
		tom.setHome(tomHome);
		tom = db.save(tom);

		Home dudHome = new Home();
		dudHome.setAddress("moon");
		dudHome.setType(HomeType.HOUSE);
		dudHome = db.save(dudHome);

		Person tomDud = new Person();
		tomDud.setName("tomdud");
		tomDud.setHome(dudHome);
		tomDud = db.save(tomDud);

		Person tomMum = new Person();
		tomMum.setName("tommum");
		tomMum.setHome(dudHome);

		tomMum = db.save(tomMum);

		Home dogHome = new Home();
		dogHome.setAddress("royal palace");
		dogHome.setType(HomeType.KENNEL);
		dogHome.setPrice(1000000);
		dogHome.setWeight(30);

		dogHome = db.save(dogHome);

		Dog tomDog = new Dog();
		tomDog.setName("cerberus");
		tomDog.setOwner(tom);
		tomDog.setHome(dogHome);
		tomDog = db.save(tomDog);

		tom.setDud(tomDud);
		tom.setMum(tomMum);
		tom.setDog(tomDog);
		tomDud.setDog(tomDog);
		
		db.save(tomDud);
		db.save(tom);
		db.commit();
		db.close();

	}
}
