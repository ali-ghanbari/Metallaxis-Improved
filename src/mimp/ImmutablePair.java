package mimp;

public class ImmutablePair<F, S> {
	protected final F fst;
	protected final S snd;

	public ImmutablePair(F first, S second) {
		this.fst = first;
		this.snd = second;
	}

	public F first() {
		return fst;
	}

	public S second() {
		return snd;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		ImmutablePair<?,?> other = (ImmutablePair<?,?>) o;
		return fst.equals(other.fst) && snd.equals(other.snd);
	}
	
	@Override
	public String toString() {
		return "(" + fst + ", " + snd + ")";
	}
}