package simulator.model;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

	private final static double INITIAL_SPEED = 0.1;
	private final static double INITIAL_ENERGY = 100.0;
	private final static double INITIAL_POS_FREC = 60.0;
	private final static double INITIAL_SIGHT_RAGE_FREC = 0.2;
	private final static double INITIAL_SPEED_FREC = 0.2;

	protected String _genetic_code;
	protected Diet _diet;
	protected State _state;
	protected Vector2D _pos;
	protected Vector2D _dest;
	protected double _energy;
	protected double _speed;
	protected double _age;
	protected double _desire;
	protected double _sight_range;
	protected Animal _mate_target;
	protected Animal _baby;
	protected AnimalMapView _region_mngr;
	protected SelectionStrategy _mate_strategy;

	protected Animal(String genetic_code, Diet diet, double sight_range, double init_speed,
			SelectionStrategy mate_strategy, Vector2D pos) throws IllegalArgumentException {
		if (genetic_code == null || genetic_code.isEmpty() || sight_range <= 0 || init_speed <= 0
				|| mate_strategy == null) {
			throw new IllegalArgumentException("Error");
		}

		this._genetic_code = genetic_code;
		this._diet = diet;
		this._sight_range = sight_range;
		this._speed = Utils.get_randomized_parameter(init_speed, INITIAL_SPEED);
		this._mate_strategy = mate_strategy;
		this._pos = pos;
		this._state = State.NORMAL;
		this._energy = INITIAL_ENERGY;
		this._desire = 0.0;

		this._dest = null;
		this._mate_target = null;
		this._baby = null;
		this._region_mngr = null;

	}

	protected Animal(Animal p1, Animal p2) {
		this._mate_target = null;
		this._baby = null;
		this._region_mngr = null;
		this._dest = null;
		this._state = State.NORMAL;
		this._desire = 0.0;
		this._genetic_code = p1._genetic_code;
		this._diet = p1._diet;
		this._mate_strategy = p2._mate_strategy;
		this._energy = (p1.get_energy() + p2.get_energy()) / 2;
		this._pos = p1.get_position()
				.plus(Vector2D.get_random_vector(-1, 1).scale(INITIAL_POS_FREC * (Utils._rand.nextGaussian() + 1)));
		this._sight_range = Utils.get_randomized_parameter((p1.get_sight_range() + p2.get_sight_range()) / 2,
				INITIAL_SIGHT_RAGE_FREC);
		this._speed = Utils.get_randomized_parameter((p1.get_speed() + p2.get_speed()) / 2, INITIAL_SPEED_FREC);
	}

	void init(AnimalMapView reg_mngr) {
		this._region_mngr = reg_mngr;
		if (_pos == null) {
			_pos = new Vector2D(Utils._rand.nextDouble() * (_region_mngr.get_width() - 1),
					Utils._rand.nextDouble() * (_region_mngr.get_height() - 1));
		} else {
			double x = _pos.getX(), y = _pos.getY();
			while (x >= _region_mngr.get_width())
				x = (x - _region_mngr.get_width());
			while (x < 0)
				x = (x + _region_mngr.get_width());
			while (y >= _region_mngr.get_height())
				y = (y - _region_mngr.get_height());
			while (y < 0)
				y = (y + _region_mngr.get_height());
			_pos = new Vector2D(x, y);
		}
		_dest = new Vector2D(Utils._rand.nextDouble() * (_region_mngr.get_width() - 1),
				Utils._rand.nextDouble() * (_region_mngr.get_height() - 1));
	}

	Animal deliver_baby() {
		Animal baby = this._baby;
		this._baby = null;
		return baby;
	}

	protected void move(double speed) {
		_pos = _pos.plus(_dest.minus(_pos).direction().scale(speed));
	}

	public JSONObject as_JSON() {
		JSONObject json = new JSONObject();
		json.put("pos", _pos);
		json.put("gcode", _genetic_code);
		json.put("diet", _diet);
		json.put("state", _state);
		return json;
	}

	@Override
	public void update(double dt) {

	}

	@Override
	public State get_state() {
		return _state;
	}

	@Override
	public Vector2D get_position() {
		return _pos;
	}

	@Override
	public String get_genetic_code() {
		return _genetic_code;
	}

	@Override
	public Diet get_diet() {
		return _diet;
	}

	@Override
	public double get_speed() {
		return _speed;
	}

	@Override
	public double get_sight_range() {
		return _sight_range;
	}

	@Override
	public double get_energy() {
		return _energy;
	}

	@Override
	public double get_age() {
		return _age;
	}

	@Override
	public Vector2D get_destination() {
		return _dest;
	}

	@Override
	public boolean is_pregnant() {
		return _baby != null;
	}
}
