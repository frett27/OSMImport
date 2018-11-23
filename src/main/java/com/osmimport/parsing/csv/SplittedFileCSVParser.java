package com.osmimport.parsing.csv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.osmimport.input.csv.CSVParser;
import com.osmimport.input.csv.ParserCallBack;
import com.osmimport.structures.model.Table;

/**
 * parse a single file or a csv partitionned folder
 * 
 * @author use
 * 
 */
public class SplittedFileCSVParser extends CSVParser {

	private static org.slf4j.Logger logger = (org.slf4j.Logger) LoggerFactory
			.getLogger(SplittedFileCSVParser.class);

	public SplittedFileCSVParser(Table table, ParserCallBack pcb) {
		super(table, pcb);
	}

	public void parse(File csvFileOrFolder) throws Exception {

		if (!csvFileOrFolder.exists()) {
			throw new Exception("file or folder " + csvFileOrFolder
					+ " does not exists");
		}

		if (csvFileOrFolder.isFile()) {

			// if file, parse it
			InputStream fis = new BufferedInputStream(new FileInputStream(
					csvFileOrFolder), 10000000);
			try {
				super.parse(fis);
			} finally {
				fis.close();
			}

		} else if (csvFileOrFolder.isDirectory()) {

			ExecutorService fixed = Executors.newFixedThreadPool(Runtime
					.getRuntime().availableProcessors());
			try {
				int cpt = 1;
				File f = new File(csvFileOrFolder, "" + cpt);
				while (f.exists() && f.isFile()) {

					final File finalf = f;

					fixed.submit(new Runnable() {
						@Override
						public void run() {
							try {
								parse(finalf);
							} catch (Exception ex) {
								logger.error(
										"error parsing file :"
												+ ex.getMessage(), ex);
							}
						}
					});

					cpt++;
					f = new File(csvFileOrFolder, "" + cpt);
				}

			} finally {
				fixed.shutdown();
				fixed.awaitTermination(10, TimeUnit.DAYS);
			}

		} else {
			throw new Exception("invalid file or folder " + csvFileOrFolder);
		}

	}
}
