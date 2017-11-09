package mimp;

public class FailureDescriptorFactory {
	private static FailureDescriptorFactory instance = null;
	
	public static final int KIND_ONLY = 1;
	public static final int TYPE_ONLY = 2;
	public static final int TYPE_AND_MSG = 3;
	public static final int FULL = 4;
	
	private int type;
	
	private FailureDescriptorFactory() {
		type = FULL;//TYPE_AND_MSG;//KIND_ONLY;//TYPE_ONLY;//
	}
	
	public static FailureDescriptorFactory v() {
		if(instance == null) {
			instance = new FailureDescriptorFactory();
		}
		return instance;
	}
	
	public void setType(int type) {
		assert(type == TYPE_ONLY || type == TYPE_AND_MSG || type == FULL);
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public FailureDescriptor create(String description) {
		switch(type) {
		case KIND_ONLY: return KindFailureDescriptor.forDescription(description);
		case TYPE_ONLY:	return TypeFailureDescriptor.forDescription(description);
		case TYPE_AND_MSG: return TypeMessageFailureDescriptor.forDescription(description);
		case FULL: return FullFailureDescriptor.forDescription(description);
		}
		throw new RuntimeException("invalid type");
	}
}
