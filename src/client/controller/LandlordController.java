package client.controller;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import client.controller.ListingsController.MyListener;
import client.view.LandlordAddView;
import client.view.LandlordEmailView;
import client.view.LandlordView;
import server.PropertyDatabaseController;
import server.UserDatabaseController;
import client.view.*;
import java.sql.*;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.core.internal.runtime.PrintStackUtil;

public class LandlordController {
	
	private ChangeStatusPopUp changeView;
	private LandlordAddView landlordAddView;
	private LandlordEmailView landlordEmailView;
	private LandlordView landlordView;
	private MyListener listener;
	private int landlordID;
	private ClientCommunicator comms; 
	private String houseTypeChoice = "--choose one--";
	private String quadChoice = "--choose one--";
	private String furnishChoice = "--choose one--";
	private String selected;
	private String selected2;
	private int rowNum;
	private String stateChoice;
	private String selectedID =" ";
	
	public LandlordController(Client c) {
		
        landlordAddView = c.landlordAddView;
        landlordEmailView = c.landlordEmailView;
        landlordView = c.landlordView;
        comms = c.communicator;
        changeView = c.changeView;
        
        listener = new MyListener();
        addListeners();
	}
	
	private void writeSocket(String s) {
		comms.socketOut.println(s);
		comms.socketOut.flush();
	}
	private String readSocket() {
		try {
			return comms.socketIn.readLine();
		}catch(Exception e) {
			System.err.println("error in read socket");
		}
		return null;
	}
	private void autoSetlandlordID() {
		writeSocket("16");
		writeSocket(landlordView.getUsername());
		String id = readSocket();
		landlordID = Integer.parseInt(id);
	}

	public class MyListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if(e.getSource()==changeView.submit) {
					System.out.println(stateChoice);
					System.out.println(selectedID);	
					if(!stateChoice.equals(" ") && !stateChoice.equals("--choose one--")) {
						String s = stateChoice+"~"+selectedID;
	
						writeSocket("13");
						writeSocket(s);
						changeView.setVisible(false);
						changeView.errorMessage("State has been changed");
						landlordView.changeStatusBtn.setEnabled(false);
						landlordView.showPropertiesBtn.doClick();
					}
					
				}
				if(e.getSource()==landlordView.changeStatusBtn) {
					changeView.setVisible(true);
				}

				if(e.getSource() == landlordView.showPropertiesBtn) {
					
					autoSetlandlordID();
					landlordView.clear();
					landlordView.setCols(new String[] {"id", "type", "bedrooms", "bathrooms", "quadrant", "furnished", "state"});
					
					writeSocket("14");
					writeSocket(Integer.toString(landlordID));

					String result = readSocket();						
					String arr[] = result.split("é");		
					for (String string : arr) {		
						String[] row = string.split("~");
						landlordView.addElementTextBox(row);
					}
					landlordView.autoColWidth();
					
				}
				
				else if(e.getSource() == landlordView.viewEmailButton) {
					
					landlordEmailView.clear();
					landlordEmailView.setCols(new String[] {"From", "Message"});
					
					writeSocket("15");
					writeSocket(Integer.toString(landlordID));
					
					String result = readSocket();										
					if(result.equals("none")) {
						landlordView.errorMessage("No Emails yet");
						return;
					}
					
					String arr[] = result.split("é");	
					for (String string : arr) {
						String[] row = string.split("~");
						landlordEmailView.addElementTextBox(row);
					}
					
					landlordEmailView.setVisible(true);
				}
				
				else if(e.getSource() == landlordEmailView.deleteBtn){
					writeSocket("10");
					writeSocket(landlordID + "é" + selected + "é" + selected2);
					landlordEmailView.deleteRow(rowNum);
					landlordEmailView.deleteBtn.setEnabled(false);
					landlordEmailView.openBtn.setEnabled(false);
				}
				
				else if(e.getSource() == landlordEmailView.openBtn) {
					landlordEmailView.displayEmail(selected2);
					landlordEmailView.openBtn.setEnabled(false);
				}
			
				else if(e.getSource() == landlordView.addPropertyBtn){
					landlordAddView.setVisible(true);
					landlordAddView.submitButton.setEnabled(false);
					landlordAddView.payButton.setEnabled(true);
				}
				else if(e.getSource() == landlordAddView.submitButton){
					String bathroom = landlordAddView.getBathrooms();
					String bedroom = landlordAddView.getBedrooms();
					int beds = -1;
					int baths = -1;
					try{
						if(!bedroom.equals(""))
							beds = Integer.parseInt(bedroom);
						if(!bathroom.equals(""))
							baths = Integer.parseInt(bathroom);
						if(beds < 1 || baths < 1)
							throw new NumberFormatException();
                    }catch(NumberFormatException a){
                    	landlordAddView.errorMessage("Please enter a valid bathroom and bedroom number");
                        return;
                    }
					if(!quadChoice.equals("--choose one--") && !houseTypeChoice.equals("--choose one--") && !furnishChoice.equals("--choose one--")){
						int id = getNewPropertyID();
						id++;
						String status = "active";
						writeSocket("9");
						writeSocket(id + "é" + houseTypeChoice + "é" + bedroom + "é" + bathroom + "é" + quadChoice +"é" + furnishChoice + "é" + landlordID + "é" + status);
						
						writeSocket("24");
						writeSocket(id + "~" + houseTypeChoice + "~" + bedroom + "~" + bathroom + "~" + quadChoice +"~" + furnishChoice + "~" + landlordID + "~" + status);
						
						landlordAddView.errorMessage("Property added");
						landlordAddView.submitButton.setEnabled(false);
						landlordAddView.ableToClose();
						landlordAddView.setVisible(false);
						landlordView.showPropertiesBtn.doClick();
					} else {
						landlordAddView.errorMessage("please select options");
					}
				}
				else if(e.getSource() == landlordAddView.payButton) {
					writeSocket("23");
					String result = readSocket();
					String arr[] = result.split("é");
					String f = arr[0];
					String p = arr[1];
					if(p.equals("Monthly")) {
						p = "month";
					}
					else {
						p = "year";
					}
					
					landlordAddView.errorMessage("Amount $" + f + " payed for this " + p);
//					landlordAddView.setVisible(false);
					landlordAddView.unableToClose();
					landlordAddView.submitButton.setEnabled(true);
					landlordAddView.payButton.setEnabled(false);
//					landlordView.showPropertiesBtn.doClick();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				System.err.println("error in Landlord controller");
			}
		}
	}	
	
	private int getNewPropertyID() {
		try {
		writeSocket("8");
		String result = readSocket();
		int currMax = Integer.parseInt(result);
		return currMax;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private void addListeners() {
		landlordEmailView.addListener(listener);
		landlordView.addListener(listener);
		changeView.addListener(listener);

		changeView.addDropdownListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
            	if(e.getStateChange() == ItemEvent.SELECTED) {
            		stateChoice = (String)e.getItem();
            	}
            }
		});
		
		landlordAddView.addSubmitListener(listener);
		landlordAddView.addPayListener(listener);
		landlordAddView.addHouseDropdownListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					houseTypeChoice = (String)e.getItem();
				}
			}
		});

		landlordAddView.addQuadDropdownListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
            	if(e.getStateChange() == ItemEvent.SELECTED) {
            		quadChoice = (String)e.getItem();
            	}
            }
        });
		landlordAddView.addfurnishDropdownListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
            	if(e.getStateChange() == ItemEvent.SELECTED) {
            		furnishChoice = (String)e.getItem();
            	}
            }
        });
		
		landlordView.addSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()&&landlordView.textBox.getSelectedRow() !=-1) {
					selectedID = landlordView.textBox.getModel().getValueAt(landlordView.textBox.getSelectedRow(),0).toString();
					 landlordView.changeStatusBtn.setEnabled(true);
				}
			}
		});
		landlordView.addListener(listener);
		landlordView.addCloseListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	System.exit(0);
            }
        });
		landlordEmailView.addSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent e) {
	        	if(!e.getValueIsAdjusting() && landlordEmailView.textBox.getSelectedRow() != -1){
	        		selected = landlordEmailView.textBox.getModel().getValueAt(landlordEmailView.textBox.getSelectedRow(),0).toString();
	        		selected2 = landlordEmailView.textBox.getModel().getValueAt(landlordEmailView.textBox.getSelectedRow(),1).toString();
	        		
	        		rowNum = landlordEmailView.textBox.getSelectedRow();
	        		landlordEmailView.deleteBtn.setEnabled(true);
	        		landlordEmailView.openBtn.setEnabled(true);
	        	}
	        }
	    });
	}
	
	
}
