Use with MQ3 sensor & Arduino Bluno Nano
Arduino code: 
const int MQ3pin = A0;
const float RL = 200000.0;       // Load resistor (ohms)
const float R0 = 572076;      // Calibrated in clean air

void setup() {
  Serial.begin(9600);
  delay(3000);  // Allow time for serial to open
}

void loop() {
  int raw = analogRead(MQ3pin);
  float Vout = raw * (5.0 / 1023.0);  // ADC to voltage

  float Rs = (5.0 - Vout) / Vout * RL;
  float ratio = Rs / R0;


  float mgL = 0.5429 * pow(ratio, -0.6663);
  if (mgL < 0) mgL = 0;

  float BAC = mgL * 0.0376;

  // // === Serial Output ===
  Serial.print("Peak BAC: "); Serial.println(BAC, 4); 

  delay(1000);
}
