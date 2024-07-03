package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

	private static final double VALUE_IN_RANGE_MIN = 0.0;
	private static final double VALUE_IN_RANGE_MAX = 100.0;
	private static final double INITIAL_SIGH_RAGE = 40.0;
	private static final double INITIAL_SPEED = 35.0;
	private static final double INITIAL_DIST = 8.0;
	private static final double INITIAL_ENERGY = 100.0;
	private static final double INITIAL_FREC = 0.007;
	private static final double PARAM_ENERGY = 20.0;
	private static final double PARAM_DESIRE = 40.0;
	private static final double MAX_DESIRE = 65.0;
	private static final double PARAM_SPEED = 2.0;
	private static final double FREC_ENERGY = 1.2;
	private static final double BABY_PROB = 0.9;

	private SelectionStrategy _danger_strategy;
	private Animal _danger_source;

	public Sheep(SelectionStrategy mate_strategy, SelectionStrategy danger_strategy, Vector2D pos) {
		super("Sheep", Diet.HERBIVORE, INITIAL_SIGH_RAGE, INITIAL_SPEED, mate_strategy, pos);
		this._mate_strategy = mate_strategy;
		this._danger_strategy = danger_strategy;
		this._pos = pos;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		this._danger_strategy = p1._danger_strategy;
		this._danger_source = null;
	}

	@Override
	public void update(double dt) {
		// paso 2.
		if (this.get_state().equals(State.NORMAL)) {
			this.setNormalState(dt);
		}
		if (this.get_state().equals(State.DANGER)) {
			this.setDangerState(dt);
		}
		if (this.get_state().equals(State.MATE)) {
			this.setMateState(dt);
		}

		// paso 3.

		double x = this.get_position().getX(), y = this.get_position().getY();
		while (x >= _region_mngr.get_width())
			x = (x - _region_mngr.get_width());
		while (x < 0)
			x = (x + _region_mngr.get_width());
		while (y >= _region_mngr.get_height())
			y = (y - _region_mngr.get_height());
		while (y < 0)
			y = (y + _region_mngr.get_height());
		_pos = new Vector2D(x, y);

		// paso 4.
		if (this.get_energy() <= 0.0 || this.get_age() > INITIAL_DIST) {
			_state = State.DEAD;
		}

		// paso 5.
		if (!this.get_state().equals(State.DEAD)) {
			_energy += _region_mngr.get_food(this, dt);
			Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
		}
		
		

	}
	
	public void setNormalState(double dt) {
		if (this.get_position().distanceTo(this.get_destination()) < INITIAL_DIST) {
			_dest = new Vector2D(Utils._rand.nextDouble() * (_region_mngr.get_width() - 1),
					Utils._rand.nextDouble() * (_region_mngr.get_height() - 1));
		}
		move(this.get_speed() * dt * Math.exp((this.get_energy() - INITIAL_ENERGY) * INITIAL_FREC));
		_age += dt;
		_energy -= PARAM_ENERGY * dt;
		Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
		_desire += PARAM_DESIRE * dt;
		Utils.constrain_value_in_range(_desire, VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);

		if (_danger_source == null) {
			/* buscar un nuevo animal que se considere como peligroso. */
			this._danger_source = _danger_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_diet().equals(Diet.CARNIVORE)));
			if (_desire > MAX_DESIRE) {
				_state = State.MATE;
			}
		} else {
			_state = State.DANGER;
		}
	}
	
	public void setDangerState(double dt) {
		if (_danger_source != null && _danger_source.get_state().equals(State.DEAD)) {
			_danger_source = null;
		}
		if (_danger_source == null) {
			if (this.get_position().distanceTo(this.get_destination()) < INITIAL_DIST) {
				_dest = new Vector2D(Utils._rand.nextDouble() * (_region_mngr.get_width() - 1),
						Utils._rand.nextDouble() * (_region_mngr.get_height() - 1));
			}
			move(this.get_speed() * dt * Math.exp((this.get_energy() - INITIAL_ENERGY) * INITIAL_FREC));
			_age += dt;
			_energy -= PARAM_ENERGY * dt;
			Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
			_desire += PARAM_DESIRE * dt;
			Utils.constrain_value_in_range(_desire, VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);

		} else {
			_dest = this.get_position().plus(this.get_position().minus(_danger_source.get_position()).direction());
			move(PARAM_SPEED * this.get_speed() * dt
					* Math.exp((this.get_energy() - INITIAL_ENERGY) * INITIAL_FREC));
			_age += dt;
			_energy -= PARAM_ENERGY * FREC_ENERGY * dt;
			Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
			_desire += PARAM_DESIRE * dt;
			Utils.constrain_value_in_range(_desire, VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);

		}
		if (_danger_source == null || this.get_position().distanceTo(_danger_source.get_position()) > this
				.get_sight_range()) {/* no está en el campo visual de un animal */
			this._danger_source = _danger_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_diet().equals(Diet.CARNIVORE)));
		}
		if (_danger_source == null) {
			if (_desire < MAX_DESIRE) {
				_state = State.NORMAL;
			} else {
				_state = State.MATE;
			}
		}
	}
	
	public void setMateState(double dt) {
		if (_mate_target != null && ((_mate_target.get_state().equals(State.DEAD))
				|| (this.get_position().distanceTo(_mate_target.get_position()) > this
						.get_sight_range()))) {/* está fuera del campo visual */
			_mate_target = null;
		}
		if (_mate_target == null) {
			/* buscar un nuevo animal para emparejarse, y si no lo encuentra avanzar */
			this._mate_target = _mate_strategy.select(this,
					_region_mngr.get_animals_in_range(this,
							animal -> animal.get_genetic_code().equals(this.get_genetic_code())
									&& animal.get_state().equals(State.MATE)));
			if (this._mate_target == null) {
				if (this.get_position().distanceTo(this.get_destination()) < INITIAL_DIST) {
					_dest = new Vector2D(Utils._rand.nextDouble() * (_region_mngr.get_width() - 1),
							Utils._rand.nextDouble() * (_region_mngr.get_height() - 1));
				}
				move(this.get_speed() * dt * Math.exp((this.get_energy() - INITIAL_ENERGY) * INITIAL_FREC));
				_age += dt;
				_energy -= PARAM_ENERGY * dt;
				Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
				_desire += PARAM_DESIRE * dt;
				Utils.constrain_value_in_range(_desire, VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
			}
		} else {
			_dest = _mate_target.get_position();
			move(PARAM_SPEED * this.get_speed() * dt
					* Math.exp((this.get_energy() - INITIAL_ENERGY) * INITIAL_FREC));
			_age += dt;
			_energy -= PARAM_ENERGY * FREC_ENERGY * dt;
			Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
			_desire += PARAM_DESIRE * dt;
			Utils.constrain_value_in_range(_desire, VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);

			if (this.get_position().distanceTo(_mate_target.get_position()) < INITIAL_DIST) {
				_desire = 0.0;
				_mate_target._desire = 0.0;
				if (_baby == null) {
					if (Utils._rand.nextDouble() < BABY_PROB) {
						_baby = new Sheep(this, _mate_target);
					}
				}
				_mate_target = null;
			}
		}
		if (_danger_source == null) {
			if (_desire < MAX_DESIRE) {
				_state = State.NORMAL;
			}
			this._danger_source = _danger_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_diet().equals(Diet.CARNIVORE)));
		} else {
			_state = State.DANGER;
		}
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