package mimp;

import java.util.HashSet;
import java.util.Set;

public abstract class Test implements Comparable<Test> {
	public final String name;
	protected Mutant[] influencers;
	protected Mutant[] failurePoints;
	protected Mutant[] cover;
	public final Set<Method> influencerMethods;
	
	protected Test(String name) {
		this.name = name;
		this.failurePoints = new Mutant[0];
		this.influencers = new Mutant[0];
		this.cover = new Mutant[0];
		this.influencerMethods = new HashSet<>();
	}
	
	public double rank() {
		//assert(influencers.length > 0);
		if(influencers.length > 0) {
			return 1. / (double) influencers.length;
		}
		return 0;
	}
	
	public void addCover(Mutant m) {
		Mutant[] cover_ext = new Mutant[cover.length + 1];
		System.arraycopy(cover, 0, cover_ext, 0, cover.length);
		cover_ext[cover.length] = m;
		cover = cover_ext;
	}
	
	public void addFailurePoint(Mutant m) {
		Mutant[] failurePoints_ext = new Mutant[failurePoints.length + 1];
		System.arraycopy(failurePoints, 0, failurePoints_ext, 0, failurePoints.length);
		failurePoints_ext[failurePoints.length] = m;
		failurePoints = failurePoints_ext;
	}
	
	public abstract void computeInfluencers();
	
	public void computeInfluencerMethods() {
		for(Mutant m : influencers) {
			influencerMethods.add(m.mutatedMethod);
		}
	}
	
	protected void addInfluencer(Mutant m) {
		Mutant[] influencers_ext = new Mutant[influencers.length + 1];
		System.arraycopy(influencers, 0, influencers_ext, 0, influencers.length);
		influencers_ext[influencers.length] = m;
		influencers = influencers_ext;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		return name.equals(((Test) o).name);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * name.hashCode();
	}
	
	@Override
	public int compareTo(Test other) {
		return name.compareTo(other.name);
	}
}
