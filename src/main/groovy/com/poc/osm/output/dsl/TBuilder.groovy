package com.poc.osm.output.dsl;

import groovy.lang.Closure;
import groovy.util.FactoryBuilderSupport;

import com.poc.osm.output.ClosureFilter;
import com.poc.osm.output.ClosureTransform;
import com.poc.osm.output.ProcessModel;
import com.poc.osm.output.Stream;
import com.poc.osm.output.model.TableHelper;
import com.poc.osm.output.dsl.FeatureClassFactory;
import com.poc.osm.output.dsl.FieldFactory;
import com.poc.osm.output.dsl.GdbFactory;
import com.poc.osm.output.dsl.OutGdbFactory;
import com.poc.osm.output.dsl.StreamFactory;
import com.poc.osm.output.dsl.TableFactory;

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

	public ProcessModel build(Stream osmentity, Closure c ) {

		processModel.mainStream = osmentity;

		c.setDelegate(this);

		Object ret = c.call(this);

		return this.processModel;
	}
}
