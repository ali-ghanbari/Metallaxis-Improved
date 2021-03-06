package mimp;

/*the general format is as follows.
exception-type[:_message]_[STACKTRACE]_meth0_meth1_..._methn*/

public abstract class FailureDescriptor {
	public final String info;
	
	protected FailureDescriptor(String info) {
		this.info = info;
	}
	
	@Override
	public String toString() {
		return info;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(this == o) {
			return true;
		}
		FailureDescriptor other = (FailureDescriptor) o;
		return info.equals(other.info);
	}
}
