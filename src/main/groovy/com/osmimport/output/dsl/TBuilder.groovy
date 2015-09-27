package com.osmimport.output.dsl;

import com.osmimport.output.ClosureFilter
import com.osmimport.output.ClosureTransform
import com.osmimport.output.ProcessModel
import com.osmimport.output.Stream
import com.osmimport.structures.model.Table

/**
 * Builder for constructing the transform pipeline
 * @author pfreydiere
 *
 */
class TBuilder extends FactoryBuilderSupport implements TableBuilderConstruct{


	protected ProcessModel processModel= new ProcessModel();

	protected Table currentTable;

	
	public TBuilder(init = true) {
		super(init);
	}
	
	Table getCurrentTable(){
		return currentTable;
	}
	
	void setCurrentTable(Table table)
	{
		currentTable = table;
	}
	



	def registerSupportNodes() {

		registerExplicitProperty("filter", 	null, { 	value ->
			Stream s = getContext()["_CURRENT_NODE_"]
			s.filter = value
		})
		registerExplicitProperty("transform", 	null , { 	value ->
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
		registerFactory("csv", new CsvFactory());
		registerFactory("out", new OutSinkFactory());

		registerFactory("table", new TableFactory());
		registerFactory("featureclass", new FeatureClassFactory());
		registerFactory("_text", new FieldFactory());
		registerFactory("_integer", new FieldFactory());
		registerFactory("_double", new FieldFactory());
		registerFactory("_long", new FieldFactory());
	}

	/**
	 * Build the process model
	 * @param osmentity
	 * @param c
	 * @return
	 */
	public ProcessModel build(Stream osmentity, Closure c ) {

		assert osmentity != null;
		processModel.mainStream = osmentity;

		c.setDelegate(this);

		Object ret = c.call(this);

		return this.processModel;
	}

	
}


