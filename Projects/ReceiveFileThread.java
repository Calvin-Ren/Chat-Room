package Project_04;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;


public class ReceiveFileThread extends Thread {
    private String fileSenderIPAddress;
    private int fileSenderPortNumber;
    private String fileName;
    private long fileSize;
    private String otherNickname;
    JFrame frame;
    JTextArea textArea;

    public ReceiveFileThread(JTextArea textArea,JFrame frame,String ip, int port_number, String file_name, long file_size, String other_nickname) {
        this.fileSenderIPAddress = ip;
        this.fileSenderPortNumber = port_number;
        this.fileName = file_name;
        this.fileSize = file_size;
        this.otherNickname = other_nickname;
        this.frame = frame;
        this.textArea = textArea;
    }

    public void GUI(String path) {
        Frame fram = new Frame();
        fram.setTitle("图片");
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        ImageIcon img = new ImageIcon(path);// 创建图片对象
        label.setIcon(img);
        panel.add(label);
        fram.add(panel);
        fram.setExtendedState(JFrame.MAXIMIZED_BOTH);// JFrame最大化
        fram.setVisible(true);// 显示JFrame
    }

    public void run() {
        Socket fileSenderSocket = null;
        try {
            fileSenderSocket = new Socket(fileSenderIPAddress, fileSenderPortNumber);
        }
        catch(IOException ex) {
            JOptionPane.showMessageDialog(frame, "无法连接到服务器接收文件!", "错误", JOptionPane.ERROR_MESSAGE);
        }
        finally {
        }
        DataInputStream getFromSender = null;
        DataOutputStream sendToSender = null;
        try {
            getFromSender = new DataInputStream(new BufferedInputStream(fileSenderSocket.getInputStream()));
            sendToSender = new DataOutputStream(new BufferedOutputStream(fileSenderSocket.getOutputStream()));
            BufferedReader getInfoFromSender =new BufferedReader(new InputStreamReader(getFromSender));

            int permit = JOptionPane.showConfirmDialog(frame, "接受文件:"+fileName+" 从 "+otherNickname+"?", "文件传输请求：", JOptionPane.YES_NO_OPTION);
            if(permit == JOptionPane.YES_OPTION) {
                sendToSender.writeBytes("accepted\n");
                sendToSender.flush();

                JFileChooser destinationFileChooser = new JFileChooser(".");
                destinationFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int status = destinationFileChooser.showSaveDialog(frame);
                File destinationPath = null;
                if (status == JFileChooser.APPROVE_OPTION) {
                    destinationPath = new File(destinationFileChooser.getSelectedFile().getPath());
                    //String destinationFile = destinationPath.getAbsolutePath()+"\\"+fileName;
                    //System.out.println(destinationFilePath);
                }
                try {
                    byte judge = getFromSender.readByte();
                    if (judge > 0) {
                        File savedFile = new File(destinationPath.getAbsolutePath()+"\\"+fileName);
                        FileOutputStream saveFileStream = new FileOutputStream(savedFile);
                        DataOutputStream fileOutput = new DataOutputStream(saveFileStream);
                        ProgressMonitorInputStream monitor = new ProgressMonitorInputStream(frame, "接受文件： "+fileName, getFromSender);
                        //ProgressMonitor progressMonitor = new ProgressMonitor(null, "Receiving "+fileName, "", 0, (int)fileSize);
                        ProgressMonitor progressMonitor = monitor.getProgressMonitor();
                        progressMonitor.setMaximum((int)fileSize);

                        int read_unit = 500;
                        int readed = 0;
                        float process = 0;
                        try {
                            while (true) {
                                byte[] data = new byte[read_unit];
                                int in = monitor.read(data);
                                readed += in;
                                process = (float) readed / fileSize * 100;
                                progressMonitor.setNote(process+" % 完成");
                                progressMonitor.setProgress(readed);
                                if (in <= 0) {
                                    break;
                                }
                                fileOutput.write(data,0,in);
                            }
                            fileOutput.flush();

                            if(savedFile.length() < fileSize) {
                                JOptionPane.showMessageDialog(frame, "传输中断!", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                            String path = destinationPath+"\\"+fileName;
                            GUI(path);
                            textArea.append("接受文件："+fileName+"    保存地址："+destinationPath+"\r\n");
                        }
                        catch(IOException e){
                            JOptionPane.showMessageDialog(frame, "传输中断!", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                        finally {
                            try {
                                fileOutput.close();
                                saveFileStream.close();
                                progressMonitor.close();
                            }
                            catch(IOException e){
                            }
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(frame, "源文件没有找到!", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch(IOException e){
                }
                finally {
                    fileSenderSocket.close();
                }
            }
            else if(permit == JOptionPane.NO_OPTION) {
                sendToSender.writeBytes("refused\n");
                sendToSender.flush();
            }
        }
        catch(IOException ex) {
        }
        finally {
        }
        //System.out.println("Receiver: "+fileReceiverSocket.getRemoteSocketAddress());
        try {
            fileSenderSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}