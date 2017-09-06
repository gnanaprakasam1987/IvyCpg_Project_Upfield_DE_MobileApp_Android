package com.ivy.sd.print;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.OrderSummary;
import com.tremol.zfplibj.FPLogger;
import com.tremol.zfplibj.ZFPLib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

//import com.tremol.zfplibj.FPLogger;


public class BtService {
   
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;
    
    private static final String NAME = "BtService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//("fa87c0d0-afac-11de-8a39-0800200c9a66")

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;    
    private ConnectThread mConnectThread;   
    private int mState;

    public static final int STATE_NONE = 6;       
    public static final int STATE_LISTEN = 7;     
    public static final int STATE_CONNECTING = 8; 
    public static final int STATE_CONNECTED =9 ;  
    
    public ZFPLib zfplib=null;
    private Resources mRes=null;
    
    public BtService(Context context, Handler handler) 
    {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mRes=context.getResources();
    }
   
    private synchronized void setState(int state)
    {
        if (D) Commons.print(TAG+ ",setState() " + mState + " -> " + state);
        mState = state;        
        mHandler.obtainMessage(OrderSummary.MESSAGE_STATE_CHANGE, state, -1)
                .sendToTarget();
    }
    public synchronized int getState() 
    {
        return mState;
    }    
    public synchronized void connect(BluetoothDevice device) 
    {
        if (D) Commons.print(TAG+ ",connect to: " + device);
        if(getState()==STATE_CONNECTED &&mConnectThread!=null)
             mConnectThread.cancel();                          
        mConnectThread = new ConnectThread(device);               
        mConnectThread.start();  
       
        setState(STATE_CONNECTING);
    }    
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) 
    {
        if (D) Commons.print(TAG+ ",connected");
       
        //if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}        
        
        setState(STATE_CONNECTED); 
        Message msg = mHandler.obtainMessage(STATE_CONNECTED);
        Bundle bundle = new Bundle();
        bundle.putString(OrderSummary.DEVICE_NAME, device.getName());
        msg.setData(bundle);

        mHandler.sendMessage(msg);
        
      
        zfplib= getLib(socket);
    }    
    public synchronized void stop() 
    {
        if (D) Commons.print(TAG+ ",stop");
        if(mConnectThread!=null)
        	mConnectThread.cancel();
        	//mConnectThread = null;        
            setState(STATE_NONE);
    }
    public synchronized void start()
    {
    	setState(STATE_LISTEN);
    }

   
 
    
    private void connectionFailed() 
    {
        setState(STATE_LISTEN);                
        Message msg = mHandler.obtainMessage(OrderSummary.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(OrderSummary.TOAST, "Unable to Connect");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }    
    private void connectionLost() 
    {
        setState(STATE_LISTEN);        
        Message msg = mHandler.obtainMessage(OrderSummary.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(OrderSummary.TOAST,"Devise connection Lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

   

    
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) 
        {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {            	
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID); // Get a BluetoothSocket for a connection with the given BluetoothDevice 
//                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}); 
//                tmp = (BluetoothSocket) m.invoke(device, 1);  //Integer.valueOf(1)    				
            }catch (Exception e)	
				{Commons.printException(TAG+ ",ConnectThrea--createRfcommSocketToServiceRecord() failed", e);}
                   
            mmSocket = tmp;
        }

        public void run() {
            Commons.print(TAG+ ",_ConnectThread.run()");setName("ConnectThread");
            mAdapter.cancelDiscovery();  
            try {    
                mmSocket.connect();                                    // This is a blocking call and will only return on a successful connection or an exception
            } catch (IOException e) {
                Commons.printException(TAG+ ",_ConnectThread.mmSocket.connect()", e);
                connectionFailed();                
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Commons.printException(TAG+ ",unable to close() socket during connection failure", e2);
                }                
                BtService.this.start();
                return;
            }
           // synchronized (BluetoothChatService.this) { 
           //     mConnectThread = null;
           // }
          
            connected(mmSocket, mmDevice); 
            
            
        }
        public void cancel() {
            try { 
            	mmSocket.getInputStream().close();
            	mmSocket.getOutputStream().close(); 
            	if(zfplib!=null)
            		zfplib.close();
            	zfplib=null;
                mmSocket.close();
               
            } catch (IOException e) {
                Commons.printException(TAG+ ",close() of connect socket failed", e);
            }
        }
    }

    
   private ZFPLib  getLib(BluetoothSocket socket){        
        final InputStream mmInStream;
        final OutputStream mmOutStream;                               
            InputStream tmpIn = null;
            OutputStream tmpOut = null;            
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Commons.printException(TAG+ ",temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            return  new ZFPLib(mmOutStream, mmInStream,mRes.getString(R.string.charset), new FPLogger() {
				
				@Override
				public void Log(String s, boolean to_fp) {
					// TODO Auto-generated method stub
					if(to_fp)
                        Commons.print("TO_FP,"+"->:"+ s);
					else
                        Commons.print("FROM_FP,"+"<-:"+ s);
				}
			});
			                
        }
                	            
}
