package sound;
/******************************************************************************
* File : MusicalNote.java
* Author : http://java.macteki.com/
* Description :
*   Play simple musical note
* Tested with : JDK 1.6
******************************************************************************/
   
class MusicalNote
{
   
  static void beep(double frequency, int duration) throws Exception
  {
       
    int nChannel = 1;         // number of channel : 1 or 2
   
    // samples per second
    float sampleRate = 16000;  // valid:8000,11025,16000,22050,44100
    int nBit = 16;             // 8 bit or 16 bit sample
   
    int bytesPerSample = nChannel*nBit/8;
   
    double durationInSecond = (double) duration/1000.0;
    int bufferSize = (int) (nChannel*sampleRate*durationInSecond*bytesPerSample);
    byte[] audioData = new byte[bufferSize];
   
    // "type cast" to ShortBuffer
    java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.wrap(audioData);
    java.nio.ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
   
   
    int sampleLength = audioData.length/bytesPerSample;
   
    // generate the sine wave
    double volume = 8192;   // 0-32767
    double PI = Math.PI;
    for(int i = 0; i < sampleLength; i++){
      double time = i/sampleRate;
      double freq = frequency;
      double angle = 2*PI*freq*time;
      double sinValue = Math.sin(angle);
   
      double fade=1;
      int decay=sampleLength*1/3;  // start fade out at 2/3 of the total time
      if (i>=sampleLength-1-decay) fade=(double)(sampleLength-1-i)/decay;
   
      short amplitude = (short) (volume*fade*sinValue);
  
      for (int c=0;c<nChannel;c++)
      {
        shortBuffer.put(amplitude);
      }
   
    }//end generating sound wave sample
   
   
    boolean isSigned=true;
    boolean isBigEndian=true;
   
    // Define audio format
    javax.sound.sampled.AudioFormat audioFormat =
      new javax.sound.sampled.AudioFormat(sampleRate, nBit, nChannel, isSigned,isBigEndian);
   
   
    javax.sound.sampled.DataLine.Info dataLineInfo =
      new javax.sound.sampled.DataLine.Info(
         javax.sound.sampled.SourceDataLine.class, audioFormat);
   
    // get the SourceDataLine object
    javax.sound.sampled.SourceDataLine sourceDataLine = 
      (javax.sound.sampled.SourceDataLine)
      javax.sound.sampled.AudioSystem.getLine(dataLineInfo);
   
    sourceDataLine.open(audioFormat);
    sourceDataLine.start();
   
    // actually play the sound
    sourceDataLine.write(audioData,0,audioData.length);
   
    // "flush",  wait until the sound is completed
    sourceDataLine.drain();
   
  }
   
     
  public static void main(String[] args) throws Exception
  {
 
    java.util.Hashtable<String,Integer> frequencyTable=
      new java.util.Hashtable<String,Integer>();
 
    frequencyTable.put("c",262);   // frequency of middle C
    frequencyTable.put("d",294);
    frequencyTable.put("e",330);
    frequencyTable.put("f",349);
    frequencyTable.put("g",392);
    frequencyTable.put("a",440);
    frequencyTable.put("b",494);
    frequencyTable.put("C",523);
    frequencyTable.put("D",587);
    frequencyTable.put("E",659);
 
    String melody="c d e f g a b C b a g f e d c c d e f g a b C b a g f e d c";
 
    for (int i=0;i<melody.length();i++)
    {
      int duration=250;
      String s=melody.substring(i,i+1);
      Integer freq = frequencyTable.get(s);
      if (freq!=null) beep(freq, duration);
    }
 
  }
}