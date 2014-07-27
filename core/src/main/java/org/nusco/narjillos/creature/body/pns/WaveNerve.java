package org.nusco.narjillos.creature.body.pns;


/**
 * Generates an output that goes from -1 to 1 and back, in a sinusoidal
 * wave. The input signal is a skew value that will be added to the current
 * output signal. For example, if you input -2 continuously, you will get a
 * wave from -3 to -1.
 * 
 * The left semiplane of the sinusoidal wave (from +90 to -90 degrees) has a
 * higher frequency than the right semiplane (from -90 to 90). This generates
 * a life-like motion, where organs move more slowly in one direction, and
 * more quickly in the other.
 */
public class WaveNerve implements Nerve {

	private static final double BEAT_RATIO = 2;

	private final double frequency;

	private double currentAngle = 0;

	public WaveNerve(double frequency) {
		this.frequency = frequency;
	}

	@Override
	public double tick(double skew) {
		return skew + getCurrentAmplitude();
	}

	private double getCurrentAmplitude() {
		double amplitude = Math.sin(currentAngle);
		currentAngle = update(currentAngle);
		return amplitude;
	}

	private double update(double currentAngle) {
		double multiplicationFactor = isInLeftSemiplane(currentAngle) ? BEAT_RATIO : 1;
		return (currentAngle + Math.PI * 2  * frequency * multiplicationFactor) % (Math.PI * 2);
	}

	private boolean isInLeftSemiplane(double currentAngle) {
		return currentAngle >= Math.PI / 2 && currentAngle < Math.PI / 2 * 3;
	}
	
	public static void main(String[] args) {
		WaveNerve nerve = new WaveNerve(1.0 / 8);
		for (int i = 0; i < 10; i++) {
			System.out.println(nerve.tick(0));
		}
	}
}
