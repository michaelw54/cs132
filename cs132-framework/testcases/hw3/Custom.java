class Main {
	public static void main(String[] a){
		int x;
		x = new B().bar();
	}
}

class A {
	int d;
	public int foo() {
		d = 2;
		System.out.println(d);
		return 0;
	}

	public int bar() {
		System.out.println(777);
		return 0;
	}
}

class B extends A {
	int d;

	public int bar() {
		d = 1;
		System.out.println(this.foo());
		System.out.println(d);
		return 0;
	}
}