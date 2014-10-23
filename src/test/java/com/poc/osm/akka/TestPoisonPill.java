package com.poc.osm.akka;

import junit.framework.TestCase;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.typesafe.config.ConfigFactory;

public class TestPoisonPill extends TestCase {

	
	public void testStop() throws Exception {
		
		
		ActorSystem sys = ActorSystem.create("mysys",ConfigFactory.parseString("akka { loglevel = \"DEBUG\" }" ) );
		
		
		ActorRef actorRef = sys.actorOf(Props.create(ActorTest.class),"a1");
		
		sys.actorOf(Props.create(ActorTest.class),"a2");

		sys.actorOf(Props.create(ActorTest.class),"a3");
		
		sys.actorOf(Props.create(ActorTest.class),"a4");
		
		sys.actorOf(Props.create(ActorTest.class),"a5");
		
		System.out.println("sending message pp");
		actorRef.tell("pp", ActorRef.noSender());
		
		
		
		sys.awaitTermination();
		
		Thread.sleep(2000);
		System.out.println("done");
		
	}
	
	
	
	
}
