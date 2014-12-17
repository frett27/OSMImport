package com.poc.osm.output.dsl;

import org.fgdbapi.thindriver.TableHelper

import com.poc.osm.output.ClosureFilter
import com.poc.osm.output.ClosureTransform
import com.poc.osm.output.ProcessModel
import com.poc.osm.output.Stream

/**
 * Builder for constructing the transform pipeline
 * @author pfreydiere
 *
 */
class TBuilder extends FactoryBuilderSupport {
	

	protected ProcessModel processModel= new ProcessModel();

	protected TableHelper currentTableHelper;


	public TBuilder(init = true) {
		super(init);
	}


	def registerSupportNodes() {
		
		registerExplicitProperty("filter", 	null, 	
			   { 	value -> 	
			           Stream s = getContext()["_CURRENT_NODE_"]
					  s.filter = value
					    })
		registerExplicitProperty("transform", 	null ,
			{ 	value ->
					Stream s = getContext()["_CURRENT_NODE_"] 
				    s.transform = value
					 })
		
		registerFactory("filter",
				new UnaryClosureFactory(clazz:ClosureFilter,
				memberName : "filter"))
		
		
		registerFactory("stream", new StreamFactory())
		
		registerFactory("transform",
				new UnaryClosureFactory(clazz:ClosureTransform,
				memberName : "transform"))
		
		registerFactory("gdb", new GdbFactory());
		registerFactory("out", new OutGdbFactory());

		registerFactory("table", new TableFactory());
		registerFactory("featureclass", new FeatureClassFactory());
		registerFactory("_text", new FieldFactory());
		registerFactory("_integer", new FieldFactory());
		registerFactory("_double", new FieldFactory());
	}

	/**
	 * Build the process model
	 * @param osmentity
	 * @param c
	 * @return
	 */
	public ProcessModel build(Stream osmentity, Closure c ) {

		processModel.mainStream = osmentity;

		c.setDelegate(this);

		Object ret = c.call(this);

		return this.processModel;
	}
}
