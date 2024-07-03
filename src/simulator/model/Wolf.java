package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

	private static final double VALUE_IN_RANGE_MIN = 0.0;
	private static final double VALUE_IN_RANGE_MAX = 100.0;
	private static final double INITIAL_SIGH_RAGE = 50.0;
	private static final double INITIAL_SPEED = 60.0;
	private static final double INITIAL_DIST = 8.0;
	private static final double INITIAL_ENERGY = 100.0;
	private static final double INITIAL_FREC = 0.007;
	private static final double PARAM_ENERGY = 18.0;
	private static final double PARAM_DESIRE = 30.0;
	private static final double MIN_ENERGY = 50.0;
	private static final double MAX_DESIRE = 65.0;
	private static final double PARAM_SPEED = 3.0;
	private static final double FREC_ENERGY = 1.2;
	private static final double ENERGY_LEFT = 10.0;
	private static final double INIT_AGE = 14.0;
	private static final double BABY_PROB = 0.9;

	private SelectionStrategy _hunting_strategy;
	private Animal _hunt_target;

	public Wolf(SelectionStrategy mate_strategy, SelectionStrategy hunting_strategy, Vector2D pos) {
		super("Wolf", Diet.CARNIVORE, INITIAL_SIGH_RAGE, INITIAL_SPEED, mate_strategy, pos);
		this._mate_strategy = mate_strategy;
		this._hunting_strategy = hunting_strategy;
		this._pos = pos;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		this._hunting_strategy = p1._hunting_strategy;
		this._hunt_target = null;
	}

	@Override
	public void update(double dt) {

		// paso2.
		if (this.get_state().equals(State.NORMAL)) {
			this.setNormalState(dt);
		}
		if (this.get_state().equals(State.HUNGER)) {
			this.setHungerState(dt);
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
		if (this.get_energy() <= 0.0 || this.get_age() > INIT_AGE) {
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

		if (this.get_energy() < MIN_ENERGY) {
			_state = State.HUNGER;
		} else {
			if (_desire > MAX_DESIRE) {
				_state = State.MATE;
			}
		}
	}
	
	public void setHungerState(double dt) {
		if (_hunt_target == null || _hunt_target.get_state().equals(State.DEAD) || this.get_position()
				.distanceTo(_hunt_target.get_position()) > this.get_sight_range()) {/* fuera del campo visual */
			/* buscar un nuevo animal para cazarlo */
			this._hunt_target = _hunting_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_diet().equals(Diet.HERBIVORE)));
		}
		if (_hunt_target == null) {
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
			_dest = _hunt_target.get_position();
			move(PARAM_SPEED * this.get_speed() * dt
					* Math.exp((this.get_energy() - INITIAL_ENERGY) * INITIAL_FREC));
			_age += dt;
			_energy -= PARAM_ENERGY * FREC_ENERGY * dt;
			Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
			_desire += PARAM_DESIRE * dt;
			Utils.constrain_value_in_range(_desire, VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);

			if (this.get_position().distanceTo(_hunt_target.get_position()) < INITIAL_DIST) {
				_hunt_target._state = State.DEAD;
				_hunt_target = null;
				_energy += MIN_ENERGY;
				Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
			}
		}
		if (this.get_energy() > MIN_ENERGY) {
			if (_desire < MAX_DESIRE) {
				_state = State.NORMAL;
			} else {
				_state = State.MATE;
			}
		}
	}
	
	public void setMateState(double dt) {
		if (_mate_target != null && (_mate_target.get_state().equals(State.DEAD)
				|| this.get_position().distanceTo(_mate_target.get_position()) > this
						.get_sight_range())) {/* está fuera del campo visual */
			_mate_target = null;
		}
		if (_mate_target == null) {
			/* buscar un nuevo animal para emparejarse, y si no lo encuentra avanzar */

			this._mate_target = _hunting_strategy.select(this,
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
						_baby = new Wolf(this, _mate_target);
					}

				}
				_energy -= ENERGY_LEFT;
				Utils.constrain_value_in_range(this.get_energy(), VALUE_IN_RANGE_MIN, VALUE_IN_RANGE_MAX);
				_mate_target = null;
			}
		}
		if (this.get_energy() < MIN_ENERGY) {
			_state = State.HUNGER;
		} else {
			if (_desire < MAX_DESIRE) {
				_state = State.NORMAL;
			}
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