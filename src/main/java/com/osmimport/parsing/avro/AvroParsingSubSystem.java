package com.osmimport.parsing.avro;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.osmimport.actors.MeasuredActor;
import com.osmimport.input.csv.ParserCallBack;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.parsing.actors.ParsingLevel;
import com.osmimport.parsing.pbf.actors.messages.MessageParsingSystemStatus;
import com.osmimport.parsing.pbf.actors.messages.MessageReadFile;
import com.osmimport.regulation.MessageRegulatorStatus;
import com.osmimport.tools.polygoncreator.IInvalidPolygonConstructionFeedBack;

import akka.actor.ActorRef;
import akka.dispatch.ControlMessage;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * Flink CSV folder parsing abstract class for defining a parsing sub system, strategy might differ
 * depending of the file
 *
 * @author pfreydiere
 */
public class AvroParsingSubSystem extends MeasuredActor {

  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  protected ActorRef dispatcher; // dispatcher

  protected ActorRef polygonDispatcher;

  protected ActorRef flowRegulator;

  private ExecutorService asyncReading = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private ExecutorService endWatching = Executors.newSingleThreadExecutor();
  

  private ActorRef output;

  private double currentVel;

  private static final RegulationMessage TICK_FOR_PID_UPDATE = new RegulationMessage();

  private static class RegulationMessage implements ControlMessage {}

  private long REG_TIME = 100;

  private IInvalidPolygonConstructionFeedBack invalidPolygonConstructionFeedBack;

  public AvroParsingSubSystem(
      ActorRef flowRegulator,
      ActorRef output,
      Long maxWaysToCreateForWorker,
      IInvalidPolygonConstructionFeedBack invalidPolygonConstructionFeedBack,
      ParsingLevel parsingLevel) {

    this.flowRegulator = flowRegulator;
    this.invalidPolygonConstructionFeedBack = invalidPolygonConstructionFeedBack;
    this.output = output;
  }

  @Override
  public void onReceiveMeasured(Object message) throws Exception {

    if (message instanceof MessageParsingSystemStatus) {

      if (MessageParsingSystemStatus.END_JOB == message) {
        log.info("terminate the process");

        // wait for all remaining events to be handled
        getContext()
            .system()
            .scheduler()
            .scheduleOnce(
                new FiniteDuration(1, TimeUnit.MINUTES),
                new Runnable() {

                  @Override
                  public void run() {
                    System.out.println("ShutDown the process");
                    getContext().system().shutdown();
                  }
                },
                getContext().system().dispatcher());
      }

    } else if (message instanceof MessageRegulatorStatus) {

      MessageRegulatorStatus s = (MessageRegulatorStatus) message;

      // the regulator tell us the new velocity
      currentVel = s.getNewVelocity();

    } else if (message instanceof MessageReadFile) {

      // start the file reading

      final MessageReadFile f = (MessageReadFile) message;

      // parser
      final OSMAvroParser parser =
          new OSMAvroParser(
              new ParserCallBack() {

                AtomicLong cpt = new AtomicLong(0);

                @Override
                public void lineParsed(long lineNumber, OSMAttributedEntity entity)
                    throws Exception {
                  if (cpt.addAndGet(1) % 1000 == 0) {
                    regulate();
                  }

                  tell(output, entity, getSelf());
                }

                @Override
                public void invalidLine(long lineNumber, String line) {

                  File file = f.getFileToRead();
                  String fileName = "<unknown>";
                  if (file != null) {
                    fileName = file.getName();
                  }
                  System.err.println(
                      "fail to parse line " + fileName + ":" + lineNumber + " :" + line);
                }
              });

      File mainFile = f.getFileToRead();
      assert mainFile != null;

      List<URL> filesToIngest = new ArrayList<>();

      if (mainFile.getName().toLowerCase().endsWith(".avrourls")) {
        // read the file and get all urls.
        LineNumberReader numberedReader = new LineNumberReader(new FileReader(mainFile));
        try {
          String l = null;
          while ((l = numberedReader.readLine()) != null) {
            l = l.replaceAll("\n", "").replaceAll("\r", ""); // sanitize
            if (!"".equals(l.trim())) {
              log.info("adding url to read :" + l.trim());
              filesToIngest.add(new URL(l));
            }
          }

        } finally {
          numberedReader.close();
        }

      } else {
        filesToIngest.add(mainFile.toURI().toURL());
      }

      for (URL ifile : filesToIngest) {

        final URL fifile = ifile;
        // read the file

        asyncReading.submit(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                try {
                  log.info("open " + fifile);
                  parser.parse(fifile.openStream());
                } catch (Exception ex) {
                  System.err.println("Exception in reading:" + ex.getMessage());
                  ex.printStackTrace();
                }

                return null;
              }
            });
      }
      
      asyncReading.shutdown();
      
      // termination
      endWatching.submit(
          new Callable<Void>() {

            @Override
            public Void call() throws Exception {
            	
              while(!asyncReading.awaitTermination(10, TimeUnit.SECONDS)) {
            	  Thread.sleep(1000);
              }
            	
              System.out.println("End of reading " + f);

              // wait for all remaining events to be handled
              getContext()
                  .system()
                  .scheduler()
                  .scheduleOnce(
                      new FiniteDuration(1, TimeUnit.MINUTES),
                      new Runnable() {
                        @Override
                        public void run() {
                          System.out.println("ShutDown the process");
                          getContext().system().shutdown();
                        }
                      },
                      getContext().system().dispatcher());

              return null;
            }
          });

    } else {
      unhandled(message);
    }
  }

  /** regulate input */
  private void regulate() {
    try {

      double v = currentVel;
      double maxTimeToWaitIf0 = 3.0;

      double timeToWaitif1000 = 0.001;

      double t = maxTimeToWaitIf0 - maxTimeToWaitIf0 / 1000.0 * v + timeToWaitif1000;
      if (t > maxTimeToWaitIf0) t = maxTimeToWaitIf0;
      if (t < 0) t = 0;

      do {

        if (t > 0.1) {
          System.out.println("Velocity :" + v);
        }
        Thread.sleep((long) (t * 1000));

      } while (currentVel < 1);

    } catch (Exception ex) {
      log.error("error in regulation :" + ex.getMessage());
      log.debug("error :" + ex.getMessage(), ex);
    }
  }

  @Override
  public void onReceive(Object message) throws Exception {

    if (TICK_FOR_PID_UPDATE.equals(message)) {
      // don't count
      getContext()
          .system()
          .scheduler()
          .scheduleOnce(
              Duration.create(REG_TIME, TimeUnit.MILLISECONDS),
              getSelf(),
              TICK_FOR_PID_UPDATE,
              getContext().dispatcher(),
              null);

      // ask for the regulation status
      tell(flowRegulator, new MessageRegulatorStatus(), getSelf());
      return;
    }

    super.onReceive(message);
  }

  @Override
  public void preStart() throws Exception {
    super.preStart();

    getContext()
        .system()
        .scheduler()
        .scheduleOnce(
            Duration.create(2, TimeUnit.SECONDS),
            getSelf(),
            TICK_FOR_PID_UPDATE,
            getContext().dispatcher(),
            null);
  }
}
