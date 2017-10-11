package mimp;

public abstract class Test implements Comparable<Test> {
	public final String name;
	protected Mutant[] influencers;
	
	protected Test(String name) {
		this.name = name;
		this.influencers = new Mutant[0];
	}
	
	public double rank() {
		return 1. / (double) influencers.length;
	}
	
	public void addInfluencer(Mutant m) {
		Mutant[] influencers_ext = new Mutant[influencers.length + 1];
		System.arraycopy(influencers, 0, influencers_ext, 0, influencers.length);
		influencers_ext[influencers.length] = m;
		this.influencers = influencers_ext;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		return name.equals(((Test) o).name);
	}
	
	@Override
	public int compareTo(Test other) {
		return name.compareTo(other.name);
	}
}
