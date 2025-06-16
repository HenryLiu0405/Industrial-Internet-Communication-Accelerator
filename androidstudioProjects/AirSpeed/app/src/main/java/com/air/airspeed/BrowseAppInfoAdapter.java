package com.air.airspeed;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.air.airspeed.AppInfo;
import com.air.airspeed.vpn.LocalVpnService;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class BrowseAppInfoAdapter extends RecyclerView.Adapter<BrowseAppInfoAdapter.ViewHolder>{

    private Context mContext;
    private List<AppInfo> mlistAppInfo;
    private int mPostion = -1;
    private View.OnClickListener mListener;
    private OnStartAppListen onStartApp;
    private VpnInterface vpnInterface;

    public BrowseAppInfoAdapter(Context context, List<AppInfo> apps)  {
        mContext = context;
        mlistAppInfo = apps;

    }
    public void setVpnInterface(Context context,VpnInterface vpninterface) {
        vpnInterface = vpninterface;
        mContext = context;
    }
   public interface VpnInterface {
      void openVpn(Context context);

       //void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
   }
    @Override
    public int getItemCount() {
        return mlistAppInfo.size();
    }

    @Override
    public long getItemId(int position)  {
        return 0;
    }

    public int getPosition() {
        return mPostion;
    }

    public void setPosition(int position) {
        this.mPostion = position;
    }

    public AppInfo getApp(int position) {
        AppInfo appInfo = mlistAppInfo.get(position);
        return appInfo;
    }

    public void removeItem(int position) {
        Log.d("removeitem", "position is " + position +"size is " + mlistAppInfo.size() );
        mlistAppInfo.remove(position);
        notifyDataSetChanged();
    }
    /*@Override
    public View getView(int position, View convertview, ViewGroup arg2) {
        View view = null;
        RecyclerView.ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {
            view = inflater.inflate(R.layout.browse_app_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else {
            view = convertview;
            holder = (RvAdapter.ViewHolder) convertview.getTag();
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawble(appInfo.getIcon());
        return view;
    }*/

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_app_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        //holder.accButton.setOnClickListener(mListener);

        holder.accButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                final AppInfo appInfo = mlistAppInfo.get(position);
                List <AppInfo>  lsApp;
                lsApp = NetApp.mInstance.getApp();
                if (lsApp == null){
                    return ;
                }

                if (holder.accButton.getText().equals("立即加速")) {
                    holder.accButton.setText("停止加速");
                    //appInfo.setIsAccelerated(true);
                    for (AppInfo sTmp : lsApp) {
                        if (appInfo.getPackName().equals(sTmp.getPackName())) {
                            sTmp.setIsAccelerated(true);
                        }
                        else{
                            sTmp.setIsAccelerated(false);
                        }
                    }

                    //onStartApp.startApp(appInfo.getPackName(),appInfo.getAppName());

                    //打开vpn
                    vpnInterface.openVpn(mContext);

                    //打开加速应用
                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(appInfo.getPackName());
                    mContext.startActivity(intent);
                    /*
                    Intent intent = new Intent(mContext,MainSpeedActivity.class);
                    Bundle bd = new Bundle();
                    bd.putString("PackageName",appInfo.getPackName());
                    bd.putString("AppName",appInfo.getAppName());
                    intent.putExtras(bd);
                    //parent.getContext().startActivity(intent);
                    mContext.startActivity(intent);*/
                } else if (holder.accButton.getText().equals("停止加速")) {
                    holder.accButton.setText("立即加速");
                    for (AppInfo sTmp : lsApp) {
                        if (appInfo.getPackName().equals(sTmp.getPackName())) {
                            sTmp.setIsAccelerated(false);
                        }
                    }
                    //appcom.closeVpn();
                }

                Log.d("Browse","have set true " + appInfo.getAppName());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppInfo appInfo = mlistAppInfo.get(position);
        holder.appIcon.setImageDrawable(appInfo.getIcon());
        if (appInfo.getIsAccelerated() == false){
            holder.accButton.setText("立即加速");
        }else{
            holder.accButton.setText("停止加速");
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mPostion = holder.getAdapterPosition();
                return false;
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final
        ImageView appIcon;
        Button accButton;
        View appView;

        public ViewHolder(View view) {
            super(view);
            appView = view;
            appIcon = (ImageView) view.findViewById(R.id.imgApp);
            accButton = (Button) view.findViewById(R.id.tvAcc);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, 0, 0, "删除");
        }
    }

    public void startOnApp(OnStartAppListen onStartApp) {
        this.onStartApp = onStartApp;
    }
    public interface OnStartAppListen {
        public void startApp(String packageName,String appName);
    }

}






















