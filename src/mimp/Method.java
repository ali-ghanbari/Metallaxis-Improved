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
	
	public double susp() {
		return mutants.stream().map(Mutant::susp).max(Double::compareTo).orElse(0.);
	}
	
	@Override
	public String toString() {
		return fullSignature;
	}
}
