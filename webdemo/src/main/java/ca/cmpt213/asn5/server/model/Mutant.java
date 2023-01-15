package ca.cmpt213.asn5.server.model;

/**
 * Represents a mutant
 */
public class Mutant {
	private static final int MAX_ABILITY = 100;
	private static final int MIN_ABILITY = 0;
	private static long nextPid = 0;

	private long pid;
	private String name;
	private int weight;
	private int height;
	private String category;
	private int ability;

	public Mutant(String name, int weight, int height, String category, int ability) {
		if (weight < 0) {
			throw new IllegalArgumentException("Weight must be non-negative");
		}

		if (height < 0) {
			throw new IllegalArgumentException("Height must be non-negative");
		}

		if (ability < MIN_ABILITY || ability > MAX_ABILITY) {
			throw new IllegalArgumentException("Ability must be between " + MIN_ABILITY + " to " + MAX_ABILITY);
		}

		this.name = name;
		this.weight = weight;
		this.height = height;
		this.category = category;
		this.ability = ability;

		this.pid = nextPid;
		nextPid++;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public String getName() {
		return name;
	}

	public int getWeight() {
		return weight;
	}

	public int getHeight() {
		return height;
	}

	public String getCategory() {
		return category;
	}

	public int getAbility() {
		return ability;
	}
}
