package org.zplayer.lyric.kugou;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class KrcText {
  private static final char[] miarry = { '@', 'G', 'a', 'w', '^', '2', 't',
      'G', 'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i' };

  public static void main(String[] args) throws IOException {
    String filenm = "E:/KuGou/source/Lyric/姜玉阳 - 爱轮回.KRC";// krc文件的全路径加文件名
    System.out.println(new KrcText().getKrcText(filenm));
  }

  /**
   * 
   * @param filenm
   *          krc文件路径加文件名
   * @return krc文件处理后的文本
   * @throws IOException
   */
  public String getKrcText(String filenm) throws IOException {
    File krcfile = new File(filenm);
    byte[] zip_byte = new byte[(int) krcfile.length()];
    FileInputStream fis = new FileInputStream(krcfile);
    byte[] top = new byte[4];
    fis.read(top);
    fis.read(zip_byte);
    fis.close();
    int j = zip_byte.length;
    for (int k = 0; k < j; k++) {
      int l = k % 16;
      int tmp67_65 = k;
      byte[] tmp67_64 = zip_byte;
      tmp67_64[tmp67_65] = (byte) (tmp67_64[tmp67_65] ^ miarry[l]);
    }
    String krc_text = new String(ZLibUtils.decompress(zip_byte), "utf-8");
    return krc_text;
  }
}