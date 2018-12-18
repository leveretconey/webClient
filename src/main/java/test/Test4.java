package test;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Test4 {
    public static void main(String[] args) {
        JFrame jf=new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(800,600);
        jf.setResizable(false);
        jf.setVisible(true);
        jf.setLayout(null);
        jf.setBackground(Color.red);

        JPanel jp=new JPanel();
        jp.setBounds(300,0,800,600);
        jf.add(jp);
        jp.setBackground(Color.BLUE);

        jp.setLayout(null);
        JButton jb=new JButton("FUUUUUUUUK");
        jb.setBounds(100,100,100,100);
        jp.add(jb);
    }
}
