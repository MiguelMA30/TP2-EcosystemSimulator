package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.misc.Utils;
import simulator.model.*;
import simulator.view.MainWindow;
import simulator.control.Controller;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;

public class Main {

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String _tag;
		private String _desc;

		private ExecMode(String modeTag, String modeDesc) {
			_tag = modeTag;
			_desc = modeDesc;
		}

		public String get_tag() {
			return _tag;
		}

		public String get_desc() {
			return _desc;
		}
	}

	// default values for some parameters
	//
	private final static Double _default_time = 10.0; // in seconds
	private final static Double _default_delta_time = 0.03;

	// some attributes to stores values corresponding to command-line parameters
	//
	public static Double _time = 0.0;
	public static Double _delta_time= 0.0;
	private static String _in_file = "resources/examples/ex1.json";
	private static String _out_file = "resources/tmp/myout.json"; // path del archivo de salida
	private static boolean simple_viewer = false; // variable para el modo viewer
	private static ExecMode _mode = ExecMode.GUI;

	//Carnivores
	private static boolean countCarnivores = false;

	//North animals
	private static boolean northAnimals = false;

	// factories
	public static Factory<Animal> animal_factory;
	public static Factory<Region> region_factory;

	private static void parse_args(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = build_options();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parse_delta_time_option(line);
			parse_help_option(line, cmdLineOptions);
			parse_mode_option(line);
			parse_carnivores_option(line);
			parse_north_option(line);
			parse_in_file_option(line);
			parse_out_file_option(line);
			parse_simple_viewer_option(line);
			parse_time_option(line);


			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static void parse_carnivores_option(CommandLine line) throws ParseException {

		if (_mode.equals(ExecMode.GUI)) {
			throw new ParseException("In GUI mode is not possible this option");
		}
		countCarnivores = line.hasOption("car");


	}

	private static void parse_north_option(CommandLine line) throws ParseException {

		if (_mode.equals(ExecMode.GUI)) {
			throw new ParseException("In GUI mode is not possible this option");
		}
		northAnimals = line.hasOption("no");
	}

	private static Options build_options() {
		Options cmdLineOptions = new Options();

		// delta-time
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg().desc(
				"A double representing actual time, in\n" + "seconds, per simulation step. Default value: " + _time)
				.build());

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// input file
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("A configuration file.").build());

		// mode
		cmdLineOptions
				.addOption(Option.builder("m").longOpt("mode").hasArg()
						.desc("Execution Mode. Possible values: 'batch' (Batch\r\n"
								+ "mode), 'gui' (Graphical User Interface mode).\r\n" + "Default value: 'gui'.")
						.build());

		// output file
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written").build());

		// simple-viewer
		cmdLineOptions.addOption(
				Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode").build());

		// steps
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("An real number representing the total simulation time in seconds. Default value: "
						+ _default_time + ".")
				.build());

		cmdLineOptions.addOption(Option.builder("car").desc("Show steps of carnivores").build());
		cmdLineOptions.addOption(Option.builder("no").desc("Show north animals").build());

		return cmdLineOptions;
	}

	private static void parse_delta_time_option(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", _default_delta_time.toString());
		try {
			_delta_time = Double.parseDouble(dt);
			assert (_delta_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for delta time: " + dt);
		}
	}

	private static void parse_help_option(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parse_in_file_option(CommandLine line) throws ParseException {
		_in_file = line.getOptionValue("i");
		if (_mode == ExecMode.BATCH && _in_file == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parse_mode_option(CommandLine line) throws ParseException {
		String mode = line.getOptionValue("m", _mode.toString());
		if (mode == null) {
			throw new ParseException("Mode cannot be null");
		}
		if(mode.equals("batch")) {
			_mode=ExecMode.BATCH;
		}
		else if(mode.equals("gui")) {
			_mode=ExecMode.GUI;
		}
		else {
			throw new ParseException("Unknown mode: " + mode);
		}
	}

	private static void parse_out_file_option(CommandLine line) throws ParseException {
		_out_file = line.getOptionValue("o");
		if (_mode == ExecMode.BATCH && _out_file == null) {
			throw new ParseException("In batch mode an output configuration file is required");
		}
	}

	private static void parse_simple_viewer_option(CommandLine line) {
		if (line.hasOption("sv")) {
			simple_viewer = true;
		}
	}

	private static void parse_time_option(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", _default_time.toString());
		try {
			_time = Double.parseDouble(t);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}

	private static void init_factories() {
		List<Builder<SelectionStrategy>> selection_strategy_builders = new ArrayList<>();
		selection_strategy_builders.add(new SelectFirstBuilder());
		selection_strategy_builders.add(new SelectClosestBuilder());
		selection_strategy_builders.add(new SelectYoungestBuilder());
		Factory<SelectionStrategy> selection_strategy_factory = new BuilderBasedFactory<SelectionStrategy>(
				selection_strategy_builders);

		List<Builder<Region>> region_builders = new ArrayList<>();
		region_builders.add(new DefaultRegionBuilder());
		region_builders.add(new DynamicSupplyRegionBuilder());
		region_factory = new BuilderBasedFactory<Region>(region_builders);

		List<Builder<Animal>> animal_builders = new ArrayList<>();
		animal_builders.add(new SheepBuilder(selection_strategy_factory));
		animal_builders.add(new WolfBuilder(selection_strategy_factory));
		animal_factory = new BuilderBasedFactory<Animal>(animal_builders);

	}

	private static JSONObject load_JSON_file(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}

	private static void start_batch_mode() throws Exception {
		InputStream is = new FileInputStream(new File(_in_file));

		// 1.
		JSONObject json = load_JSON_file(is);

		int w = json.getInt("width");
		int h = json.getInt("height");
		int c = json.getInt("rows");
		int r = json.getInt("cols");

		is.close();




		// 2.
		OutputStream out = new FileOutputStream(_out_file);

		// 3.

		Simulator simulator = new Simulator(r, c, w, h, animal_factory, region_factory);

		CountCarnivoresObserver carnivoresObserver = null;
		if (countCarnivores) {
			carnivoresObserver = new CountCarnivoresObserver();
			simulator.addObserver(carnivoresObserver);
		}

		NorthAnimalsObserver northAnimalsObserver = null;
		if (northAnimals) {
			northAnimalsObserver = new NorthAnimalsObserver();
			simulator.addObserver(northAnimalsObserver);
		}


		// 4.
		Controller controller = new Controller(simulator);

	
		// 5.
		controller.load_data(json);

		// 6.
		controller.run(_time, _delta_time, simple_viewer, out);

		if (countCarnivores) {
			carnivoresObserver.printResults();
		}
		if (northAnimals) {
			northAnimalsObserver.printResults();
		}

		// 7.
		out.close();
	}






	private static void start_GUI_mode() throws Exception {

		Simulator simulator;
		Controller controller;

		if (_in_file != null) {
			InputStream is = new FileInputStream(new File(_in_file));

			JSONObject json = load_JSON_file(is);

			int w = json.getInt("width");
			int h = json.getInt("height");
			int c = json.getInt("rows");
			int r = json.getInt("cols");

			is.close();
			simulator = new Simulator(r, c, w, h, animal_factory, region_factory);
			controller = new Controller(simulator);
			controller.load_data(json);
		} else {
			simulator = new Simulator(800, 600, 20, 15, animal_factory, region_factory);
			controller = new Controller(simulator);

		}

		SwingUtilities.invokeAndWait(() -> new MainWindow(controller));

	}

	private static void start(String[] args) throws Exception {
		init_factories();
		parse_args(args);
		switch (_mode) {
		case BATCH:
			start_batch_mode();
			break;
		case GUI:
			start_GUI_mode();
			break;
		}
	}

	public static void main(String[] args) {
		Utils._rand.setSeed(2147483647l);
		try {
			start(args);
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}
}