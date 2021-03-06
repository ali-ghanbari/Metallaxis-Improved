package mimp;

import java.util.ArrayList;
import java.util.List;

public class Method implements Comparable<Method> {
	public final String fullSignature;
	public final List<Mutant> mutants;
	
	public Method(String fullSignature) {
		this.fullSignature = fullSignature;
		this.mutants = new ArrayList<>();
	}

	@Override
	public int compareTo(Method other) {
		return fullSignature.compareTo(other.fullSignature);
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		Method other = (Method) o;
		return fullSignature.equals(other.fullSignature);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * fullSignature.hashCode();
	}
	
	public double oldSusp() {
		return mutants.stream().map(Mutant::oldSusp).max(Double::compareTo).orElse(0.);
	}
	
	public double newSusp() {
		return mutants.stream().map(Mutant::newSusp).max(Double::compareTo).orElse(0.);
	}
	
	public int numberOfMutants() {
		return mutants.size();
	}
	
	@Override
	public String toString() {
		return fullSignature;
	}
}
