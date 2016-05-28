package sound;

import javax.sound.sampled.Clip;

/******************************************************************************
* File : FadingWavePlayer.java
* Author : Ivan Macteki        Link : http://java.macteki.com/
* Description :
*   Play a 16-bit sound file with fading out effect
*   You may also look at FadeBeep.java at
*     http://java.macteki.com/2011/04/sound-programming-part-2.html
* Tested with : JDK 7 update 15
******************************************************************************/
 
class FadingWavePlayer
{
  // change the filename to any wave file in your harddisk.
  static String filename="C:/Users/pedro/Music/adios.wav";
 
  // Limitation: work for 16-bit sample only  
  static void fadeOut(byte[] buf,boolean isBigEndian)
  {
    int sampleLength = buf.length/2;  // assuming 16 bit sample
    for(int i = 0; i < sampleLength; i++)
    {
      double fade=1;
      int decay=sampleLength*1/8;  // start fade out at 1/4 of the total time
      if (i>=sampleLength-1-decay) fade=(double)(sampleLength-1-i)/decay;
   
      short amplitude;  // assuming 16 bit sample
      // convert next 2 bytes to unsigned value
      int low=(buf[i*2] + 256) & 255;     
      int high=(buf[i*2+1] + 256) & 255;
      if (isBigEndian)
      {
        int t=low; low=high; high=t; // swap
      }
 
      // combine 2 bytes to form a 16 bit sample
      amplitude=(short) (high<<8 | low);
      // fade out according to its position in the buffer
      amplitude = (short) (amplitude*fade);
 
      if (isBigEndian)
      {
        buf[i*2]=(byte) (amplitude>>>8);
        buf[i*2+1]=(byte) (amplitude & 0xff);
      }
      else
      {
        buf[i*2+1]=(byte) (amplitude>>>8);
        buf[i*2]=(byte) (amplitude & 0xff);
      }
    }//end modifying sound wave sample
  }
 
  public static void main(String[] args) throws Exception
  {
    // create a AudioInputStream from the wave file
    java.io.File soundFile = new java.io.File(filename);
    javax.sound.sampled.AudioInputStream stream =
      javax.sound.sampled.AudioSystem.getAudioInputStream(soundFile);
 
    // read the audio format from the file.
    // AudioFormat include info such as :
    //   sampleRate, nBit, nChannel, isSigned,isBigEndian
    // Since the format is already stored in the wave file, 
    // we don't have to define those variables like FadeBeep.java
    javax.sound.sampled.AudioFormat audioFormat = stream.getFormat();
 
    // create a Info object according to the format
    javax.sound.sampled.DataLine.Info dataLineInfo = 
      new javax.sound.sampled.DataLine.Info(
        javax.sound.sampled.SourceDataLine.class, audioFormat
      );
 
    // Create a DataLine object according to the info above.
    javax.sound.sampled.SourceDataLine dataLine= 
      (javax.sound.sampled.SourceDataLine)
      javax.sound.sampled.AudioSystem.getLine(dataLineInfo);
 
    // read the whole audio data into a byte buffer
    int size=(int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
    dataLine.open(audioFormat, size);
    dataLine.start();
    byte[] buf=new byte[size];
    int bytesRead=0;
    while (bytesRead<size)
      bytesRead+=stream.read(buf,bytesRead,size-bytesRead);
 
    // add fade out effect to the buffer
    fadeOut(buf,audioFormat.isBigEndian());
 
    // play the sound
    dataLine.write(buf,0, size);
 
    // and wait for finish
    dataLine.drain();
 
    // print out the info for debugging
    System.out.println(dataLineInfo);
  }
}
