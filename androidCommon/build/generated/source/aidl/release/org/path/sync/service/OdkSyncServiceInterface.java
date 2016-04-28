/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\bserda\\AndroidStudioProjects\\EpiSample\\androidCommon\\src\\main\\aidl\\org\\path\\sync\\service\\OdkSyncServiceInterface.aidl
 */
package org.path.sync.service;
public interface OdkSyncServiceInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.path.sync.service.OdkSyncServiceInterface
{
private static final java.lang.String DESCRIPTOR = "org.path.sync.service.OdkSyncServiceInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.path.sync.service.OdkSyncServiceInterface interface,
 * generating a proxy if needed.
 */
public static org.path.sync.service.OdkSyncServiceInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.path.sync.service.OdkSyncServiceInterface))) {
return ((org.path.sync.service.OdkSyncServiceInterface)iin);
}
return new org.path.sync.service.OdkSyncServiceInterface.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getSyncStatus:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.path.sync.service.SyncStatus _result = this.getSyncStatus(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_synchronize:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _result = this.synchronize(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_push:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.push(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getSyncProgress:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.path.sync.service.SyncProgressState _result = this.getSyncProgress(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getSyncUpdateMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getSyncUpdateMessage(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.path.sync.service.OdkSyncServiceInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public org.path.sync.service.SyncStatus getSyncStatus(java.lang.String appName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.path.sync.service.SyncStatus _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appName);
mRemote.transact(Stub.TRANSACTION_getSyncStatus, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.path.sync.service.SyncStatus.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean synchronize(java.lang.String appName, boolean deferInstanceAttachments) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appName);
_data.writeInt(((deferInstanceAttachments)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_synchronize, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean push(java.lang.String appName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appName);
mRemote.transact(Stub.TRANSACTION_push, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public org.path.sync.service.SyncProgressState getSyncProgress(java.lang.String appName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.path.sync.service.SyncProgressState _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appName);
mRemote.transact(Stub.TRANSACTION_getSyncProgress, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.path.sync.service.SyncProgressState.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getSyncUpdateMessage(java.lang.String appName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appName);
mRemote.transact(Stub.TRANSACTION_getSyncUpdateMessage, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getSyncStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_synchronize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_push = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getSyncProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getSyncUpdateMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public org.path.sync.service.SyncStatus getSyncStatus(java.lang.String appName) throws android.os.RemoteException;
public boolean synchronize(java.lang.String appName, boolean deferInstanceAttachments) throws android.os.RemoteException;
public boolean push(java.lang.String appName) throws android.os.RemoteException;
public org.path.sync.service.SyncProgressState getSyncProgress(java.lang.String appName) throws android.os.RemoteException;
public java.lang.String getSyncUpdateMessage(java.lang.String appName) throws android.os.RemoteException;
}
