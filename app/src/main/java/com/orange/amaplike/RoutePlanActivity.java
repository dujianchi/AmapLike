package com.orange.amaplike;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.Path;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.orange.amaplike.adapter.BusResultListAdapter;
import com.orange.amaplike.adapter.DrivePathAdapter;
import com.orange.amaplike.adapter.RideStepAdapter;
import com.orange.amaplike.adapter.WalkStepAdapter;
import com.orange.amaplike.behavior.NoAnchorBottomSheetBehavior;
import com.orange.amaplike.overlay.AMapServicesUtil;
import com.orange.amaplike.overlay.AMapUtil;
import com.orange.amaplike.overlay.BusRouteOverlay;
import com.orange.amaplike.overlay.DrivingRouteOverlay;
import com.orange.amaplike.overlay.RideRouteOverlay;
import com.orange.amaplike.overlay.WalkRouteOverlay;
import com.orange.amaplike.pickpoi.PoiItemEvent;
import com.orange.amaplike.pickpoi.PoiSearchActivity;
import com.orange.amaplike.pickpoi.SelectedMyPoiEvent;
import com.orange.amaplike.utils.ImageUtil;
import com.orange.amaplike.utils.ViewAnimUtils;
import com.orange.amaplike.widget.PickDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.dujc.core.ui.BaseActivity;
import cn.dujc.core.util.Numbers;

/**
 * created by czh on 2018/1/4
 * 高德地图路径规划
 */
public class RoutePlanActivity extends BaseActivity implements RouteSearch.OnRouteSearchListener, BusResultListAdapter.BusListItemListner {

    public static final String CITY_CODE = "CityCode";
    private final int HISTORY_SIZE = 10;

    @BindView(R.id.topLayout)
    RelativeLayout mTopLayout;
    @BindView(R.id.route_plan_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.top_search_layout)
    LinearLayout mTopSearchLayout;
    @BindView(R.id.route_plan_map)
    TextureMapView mMapView;
    @BindView(R.id.route_plan_loca_btn)
    ImageView mImageViewBtn;
    @BindView(R.id.coordinatorlayout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.route_plan_from_edit)
    TextView mFromText;
    @BindView(R.id.route_plan_to_edit)
    TextView mTargetText;
    @BindView(R.id.bus_result_recyclerView)
    RecyclerView mBusResultRview;

    @BindView(R.id.tv_limit1)
    TextView tv_limit1;
    @BindView(R.id.et_limit2)
    EditText et_limit2;
    @BindView(R.id.tv_car_model)
    TextView tv_car_model;

    @BindView(R.id.route_plan_float_btn)
    FloatingActionButton mFloatBtn;
    @BindView(R.id.sheet_head_layout)
    LinearLayout mSheetHeadLayout;
    @BindView(R.id.route_plan_poi_title)
    TextView mPoiTitleText;
    @BindView(R.id.bottom_sheet)
    NestedScrollView mNesteScrollView;
    @BindView(R.id.route_plan_poi_desc)
    TextView mPoiDescText;
    @BindView(R.id.route_plan_poi_detail_layout)
    LinearLayout mPoiDetailLayout;
    @BindView(R.id.path_detail_recyclerView)
    RecyclerView mPathDetailRecView;
    @BindView(R.id.path_detail_traffic_light_text)
    TextView mPathTipsText;
    @BindView(R.id.navi_start_btn_1)
    TextView mNaviText;
    @BindView(R.id.navi_start_btn)
    Button mNaviBtn;

    @BindView(R.id.path_layout)
    LinearLayout mPathLayout;
    @BindView(R.id.path_layout1)
    LinearLayout mPathLayout1;
    @BindView(R.id.path_layout2)
    LinearLayout mPathLayout2;
    @BindView(R.id.path_general_time)
    TextView mPathDurText;
    @BindView(R.id.path_general_time1)
    TextView mPathDurText1;
    @BindView(R.id.path_general_time2)
    TextView mPathDurText2;
    @BindView(R.id.path_general_distance)
    TextView mPathDisText;
    @BindView(R.id.path_general_distance1)
    TextView mPathDisText1;
    @BindView(R.id.path_general_distance2)
    TextView mPathDisText2;

    private NoAnchorBottomSheetBehavior mBehavior;
    private BusResultListAdapter mBusResultAdapter;
    private DrivePathAdapter mDrivePathAdapter;
    private RideStepAdapter mRideStepAdapter;
    private WalkStepAdapter mWalkStepAdapter;


    private static final int MSG_MOVE_CAMERA = 0x01;
    private final int TYPE_DRIVE = 100;
    private final int TYPE_BUS = 101;
    private final int TYPE_WALK = 102;
    private final int TYPE_RIDE = 103;
    private int mSelectedType = TYPE_DRIVE;

    private int mTopLayoutHeight = 200;


    private float mDegree = 0f;
    private SensorManager mSensorManager;

    private boolean FirstLocate = true;
    private AMap mAmap;
    private MyLocationStyle mLocationStyle;
    private Poi mEndPoi;
    private Poi mStartPoi;
    private DriveRouteResult mDriveRouteResult;
    private BusRouteResult mBusRouteResult;
    private WalkRouteResult mWalkRouteResult;
    private RideRouteResult mRideRouteResult;

    private AMapNavi mAMapNavi;

    private Handler mHandlerr = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MOVE_CAMERA:
                    mAmap.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng) msg.obj, 15));
                    break;
            }
        }
    };

    @Override
    public int getViewId() {
        return R.layout.activity_route_plan;
    }

    @Override
    public void initBasic(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);

        initView();
        initTabLayout();

        initMap(savedInstanceState);
        initSensor();
        initSheet();

        permissionKeeper().requestPermissionsNormal(1
                , Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE);
//        updateUiAfterRouted();
    }

    @Override
    public void onGranted(int requestCode, List<String> permissions) {
    }

    private void initView() {
        /**   公交页面   **/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBusResultRview.setLayoutManager(linearLayoutManager);


        mTopLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTopLayoutHeight = mTopLayout.getHeight();
                mTopLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void initSheet() {
        mBehavior = NoAnchorBottomSheetBehavior.from(mNesteScrollView);
        mBehavior.setState(NoAnchorBottomSheetBehavior.STATE_COLLAPSED);
        mBehavior.setPeekHeight(getSheetHeadHeight());
        mBehavior.setBottomSheetCallback(new NoAnchorBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (mTopLayout.getVisibility() == View.VISIBLE && mPathDetailRecView.getVisibility() == View.VISIBLE) {
                    if (slideOffset > 0.5) {
                        mNaviText.setVisibility(View.GONE);
                        mNaviBtn.setVisibility(View.VISIBLE);
                    } else {
                        mNaviText.setVisibility(View.VISIBLE);
                        mNaviBtn.setVisibility(View.GONE);
                    }
                }
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mPathDetailRecView.setLayoutManager(linearLayoutManager);
    }

    private void initTabLayout() {
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_drive));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_bus));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_walk));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_ride));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (getString(R.string.route_plan_drive).equals(tab.getText())) {
                    mSelectedType = TYPE_DRIVE;
                    if (mEndPoi == null) {
                        return;
                    }
                    Location myLocation = mAmap.getMyLocation();
                    routeSearch(mStartPoi, mEndPoi, TYPE_DRIVE);
                } else if (getString(R.string.route_plan_bus).equals(tab.getText())) {
                    mSelectedType = TYPE_BUS;
                    if (mEndPoi == null) {
                        return;
                    }
                    routeSearch(mStartPoi, mEndPoi, TYPE_BUS);
                } else if (getString(R.string.route_plan_walk).equals(tab.getText())) {
                    mSelectedType = TYPE_WALK;
                    if (mEndPoi == null) {
                        return;
                    }
                    Location location = mAmap.getMyLocation();
                    routeSearch(mStartPoi, mEndPoi, TYPE_WALK);
                } else if (getString(R.string.route_plan_ride).equals(tab.getText())) {
                    mSelectedType = TYPE_RIDE;
                    if (mEndPoi == null) {
                        return;
                    }
                    Location location = mAmap.getMyLocation();
                    routeSearch(mStartPoi, mEndPoi, TYPE_RIDE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initMap(Bundle savedInstanceState) {
        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        long t1 = System.currentTimeMillis();
        mMapView.onCreate(savedInstanceState);
        Log.d("czh", System.currentTimeMillis() - t1 + "ms");
        mAmap = mMapView.getMap();

        /**   基本设置   **/
        mAmap.setTrafficEnabled(true);
        mAmap.showIndoorMap(true);
        UiSettings uiSettings = mAmap.getUiSettings();
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_CENTER);

        /**   定位模式   **/
        mLocationStyle = new MyLocationStyle();
        mLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        mLocationStyle.interval(2000);
        mAmap.setMyLocationStyle(mLocationStyle);
        mAmap.setMyLocationEnabled(true);
        mAmap.animateCamera(CameraUpdateFactory.zoomTo(15));

        /**   监听   **/
        mAmap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (FirstLocate) {
                    FirstLocate = false;
                    LocaBtnOnclick();
                    mStartPoi = new Poi(getString(R.string.poi_search_my_location),
                            new LatLng(location.getLatitude(), location.getLongitude()), "");
                    updateEditUI();
                }
            }
        });
        mAmap.setOnPOIClickListener(new AMap.OnPOIClickListener() {
            @Override
            public void onPOIClick(Poi poi) {
                onPoiClick(poi);
            }
        });
        mAmap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
//                xLog.D("onCameraChange");
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
//                mBtnState=BTN_STATE_NOR;
//                LocateBtnUIChagen();
            }
        });
        mAmap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
        mAmap.setAMapGestureListener(new AMapGestureListener() {
            @Override
            public void onDoubleTap(float v, float v1) {
            }

            @Override
            public void onSingleTap(float v, float v1) {
            }

            @Override
            public void onFling(float v, float v1) {
            }

            @Override
            public void onScroll(float v, float v1) {
            }

            @Override
            public void onLongPress(float v, float v1) {
            }

            @Override
            public void onDown(float v, float v1) {
            }

            @Override
            public void onUp(float v, float v1) {
                mBtnState = BTN_STATE_NOR;
                LocateBtnUIChagen();
            }

            @Override
            public void onMapStable() {
            }
        });
    }

    private SensorEventListener mSensorListner = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float degree = event.values[0];
            mDegree = degree;
//            xLog.D("degree:"+mDegree);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListner,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }


    private final int BTN_STATE_NOR = 100;
    private final int BTN_STATE_LOCATE = 101;
    private final int BTN_STATE_DIRE = 102;

    private int mBtnState = BTN_STATE_NOR;

    @OnClick(R.id.route_plan_loca_btn)
    public void LocaBtnOnclick() {
        if (mBtnState == BTN_STATE_NOR) {
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
            changeMapLevelAndAngle(16, 0);
            mBtnState = BTN_STATE_LOCATE;
            LocateBtnUIChagen();
        } else if (mBtnState == BTN_STATE_LOCATE) {
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
            changeMapLevelAndAngle(18, 40);
            mBtnState = BTN_STATE_DIRE;
            LocateBtnUIChagen();
        } else if (mBtnState == BTN_STATE_DIRE) {
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
            changeMapLevelAndAngle(16, 0);
            mBtnState = BTN_STATE_NOR;
            LocateBtnUIChagen();
        }
    }

    private void changeMapLevelAndAngle(final int lv, final int angle) {
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mAmap.getMyLocation().getLatitude(), mAmap.getMyLocation().getLongitude())
                , lv);
        mAmap.animateCamera(mCameraUpdate, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {
                mAmap.animateCamera(CameraUpdateFactory.changeTilt(angle), new AMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (lv > 17) {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.changeBearing(mDegree);
                            mAmap.animateCamera(cameraUpdate);
                        } else {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.changeBearing(0);
                            mAmap.animateCamera(cameraUpdate);
                        }

                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }

            @Override
            public void onCancel() {
            }
        });
    }


    /**
     * 选点返回处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectPoiEvent(PoiItemEvent event) {
        PoiItem item = event.getItem();
        LatLonPoint point = item.getLatLonPoint();
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        if (event.getFrom() == PoiSearchActivity.FROM_START) {
            mStartPoi = new Poi(item.getTitle(), latLng, item.getAdName());
        } else if (event.getFrom() == PoiSearchActivity.FROM_TARGET) {
            mEndPoi = new Poi(item.getTitle(), latLng, item.getAdName());
        }
        updateEditUI();
//        goToPlaceAndMark(item);

        mPoiTitleText.setText(item.getTitle());
        mPoiDescText.setText(item.getAdName() + "    " + item.getSnippet());

        if (mStartPoi == null || mEndPoi == null) {
            return;
        }
        routeSearch(mStartPoi, mEndPoi, mSelectedType);
    }


    /**
     * 点击我的位置处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectedMyPoiEvent(SelectedMyPoiEvent event) {
        Location location = mAmap.getMyLocation();
        if (event.getFrom() == PoiSearchActivity.FROM_START) {
            mStartPoi = new Poi(getString(R.string.poi_search_my_location),
                    new LatLng(location.getLatitude(), location.getLongitude()), "");
        } else if (event.getFrom() == PoiSearchActivity.FROM_TARGET) {
            mEndPoi = new Poi(getString(R.string.poi_search_my_location),
                    new LatLng(location.getLatitude(), location.getLongitude()), "");
        }
        updateEditUI();
    }

    /**
     * 地图poi点击
     *
     * @param poi
     */
    private void onPoiClick(Poi poi) {
        goToPlace(poi.getCoordinate());
        mEndPoi = poi;
        mTargetText.setText(poi.getName());
        mPoiTitleText.setText(poi.getName());
        mPoiDescText.setText(poi.toString());
        mPathLayout.setVisibility(View.GONE);
        mPathLayout1.setVisibility(View.GONE);
        mPathLayout2.setVisibility(View.GONE);
        mPathTipsText.setVisibility(View.GONE);
        mPathDetailRecView.setVisibility(View.GONE);
        mNaviBtn.setVisibility(View.GONE);
        mNaviText.setVisibility(View.GONE);
        mPoiDetailLayout.setVisibility(View.VISIBLE);
        mNesteScrollView.setVisibility(View.VISIBLE);
        mFloatBtn.show();
        mBehavior.setPeekHeight(getSheetHeadHeight());
        mAmap.clear();
        mAmap.addMarker(new MarkerOptions().position(poi.getCoordinate()).title(poi.getName()));
    }

    private void goToPlace(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        mAmap.animateCamera(cameraUpdate);
    }


    private LatLng getLatLngFromLocation() {
        Location location = mAmap.getMyLocation();
        return new LatLng(location.getLatitude(), location.getLongitude());
    }


    private void goToPlaceAndMark(PoiItem item) {
        LatLonPoint point = item.getLatLonPoint();
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mAmap.clear();
        mAmap.animateCamera(cameraUpdate);
        mAmap.addMarker(new MarkerOptions().position(latLng).title(item.getTitle()));
    }

    private void LocateBtnUIChagen() {
        if (mBtnState == BTN_STATE_NOR) {
            mImageViewBtn.setImageResource(R.drawable.icon_c34);
        } else if (mBtnState == BTN_STATE_LOCATE) {
            mImageViewBtn.setImageResource(R.drawable.icon_c34_b);
        } else if (mBtnState == BTN_STATE_DIRE) {
            mImageViewBtn.setImageResource(R.drawable.icon_c34_a);
        }
    }

    @OnClick({R.id.route_plan_return_btn, R.id.top_search_layout, R.id.route_plan_start_edit_layout, R.id.route_plan_to_edit_layout,
            R.id.route_plan_exchange_btn, R.id.route_plan_float_btn, R.id.navi_start_btn, R.id.navi_start_btn_1,
            R.id.path_layout, R.id.path_layout1, R.id.path_layout2, R.id.tv_car_model
    })
    public void onViewclik(View view) {
        if (FirstLocate) {
            return;
        }
        switch (view.getId()) {
            case R.id.route_plan_return_btn:
                finish();
                break;
            case R.id.top_search_layout:
                Location location3 = mAmap.getMyLocation();
                PoiSearchActivity.start(mActivity, location3.getExtras(), location3.getLatitude(), location3.getLongitude(), PoiSearchActivity.FROM_TARGET);
                break;
            case R.id.route_plan_start_edit_layout:
                Location location1 = mAmap.getMyLocation();
                PoiSearchActivity.start(mActivity, location1.getExtras(), location1.getLatitude(), location1.getLongitude(), PoiSearchActivity.FROM_START);
                break;
            case R.id.route_plan_to_edit_layout:
//                startActivity(new Intent(mActivity, BusActivity.class));
                Location location2 = mAmap.getMyLocation();
                PoiSearchActivity.start(mActivity, location2.getExtras(), location2.getLatitude(), location2.getLongitude(), PoiSearchActivity.FROM_TARGET);
                break;
            case R.id.route_plan_exchange_btn:
                Poi temp = mStartPoi;
                mStartPoi = mEndPoi;
                mEndPoi = temp;
                updateEditUI();
                routeSearch(mStartPoi, mEndPoi, mSelectedType);
                break;
            case R.id.route_plan_float_btn:
                if (mEndPoi == null) {
                    return;
                }
                routeSearch(mStartPoi, mEndPoi, mSelectedType);
                break;
            case R.id.navi_start_btn:
            case R.id.navi_start_btn_1:
                if (mEndPoi == null) {
                    return;
                }
                //todo
                starter()
                        .with("limit", et_limit2.getText().toString())
                        .go(RouteNaviActivity.class);
                //new NaviDialog().showView(mActivity, mStartPoi, mEndPoi, mSelectedType);
                break;
            case R.id.path_layout:
                onPathClick(0);
                break;
            case R.id.path_layout1:
                onPathClick(1);
                break;
            case R.id.path_layout2:
                onPathClick(2);
                break;
            case R.id.tv_car_model:
                showPickDialog();
                break;
        }
    }

    PickDialog mPickDialog;

    private void showPickDialog() {
        if (mPickDialog == null) {
            mPickDialog = new PickDialog(mActivity, new PickDialog.Callback() {
                @Override
                public void onPicked(String name, double val) {
                    tv_car_model.setText(name);
                    String text = String.valueOf(val);
                    tv_limit1.setText(text);
                    et_limit2.setText(text);
                }
            });
        }
        if (!mPickDialog.isShowing()) mPickDialog.show();
    }

    private void onPathClick(int i) {
        switch (mSelectedType) {
            case TYPE_DRIVE:
                mPathTipsText.setText(getString(R.string.route_plan_path_traffic_lights, mDriveRouteResult.getPaths().get(i).getTotalTrafficlights() + ""));
                mPathDetailRecView.setAdapter(new DrivePathAdapter(mActivity, mDriveRouteResult.getPaths().get(i).getSteps()));
                drawDriveRoutes(mDriveRouteResult, mDriveRouteResult.getPaths().get(i));
                break;
            case TYPE_WALK:
                mPathDetailRecView.setAdapter(new WalkStepAdapter(mActivity, mWalkRouteResult.getPaths().get(i).getSteps()));
                drawWalkRoutes(mWalkRouteResult, mWalkRouteResult.getPaths().get(i));
                break;
            case TYPE_RIDE:
                mPathDetailRecView.setAdapter(new RideStepAdapter(mActivity, mRideRouteResult.getPaths().get(i).getSteps()));
                drawRideRoutes(mRideRouteResult, mRideRouteResult.getPaths().get(i));
                break;
            default:
                break;
        }
    }


    private void updateEditUI() {
        if (mStartPoi == null) {
            mFromText.setText("");
//            mFromText.setText(getString(R.string.poi_search_my_location));
        } else {
            mFromText.setText(mStartPoi.getName());
        }
        if (mEndPoi == null) {
            mTargetText.setText("");
        } else {
            mTargetText.setText(mEndPoi.getName());
        }
    }

    private void updatePathGeneral(Path path, int i) {
        String dur = AMapUtil.getFriendlyTime((int) path.getDuration());
        String dis = AMapUtil.getFriendlyLength((int) path.getDistance());
        if (i == 0) {
            mPathDurText.setText(dur);
            mPathDisText.setText(dis);
            mPathLayout.setVisibility(View.VISIBLE);
            mPathLayout1.setVisibility(View.GONE);
            mPathLayout2.setVisibility(View.GONE);
        } else if (i == 1) {
            mPathDurText1.setText(dur);
            mPathDisText1.setText(dis);
            mPathLayout.setVisibility(View.VISIBLE);
            mPathLayout1.setVisibility(View.VISIBLE);
            mPathLayout2.setVisibility(View.GONE);

        } else if (i == 2) {
            mPathDurText2.setText(dur);
            mPathDisText2.setText(dis);
            mPathLayout.setVisibility(View.VISIBLE);
            mPathLayout1.setVisibility(View.VISIBLE);
            mPathLayout2.setVisibility(View.VISIBLE);
        }
    }


    private int getSheetHeadHeight() {
        mSheetHeadLayout.measure(0, 0);
        Log.d("czh", mSheetHeadLayout.getMeasuredHeight() + "height");
        return mSheetHeadLayout.getMeasuredHeight();
    }

    private int getTopLayoutHeight() {
//        RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) mTopLayout.getLayoutParams();
        Log.d("czh", mTopLayoutHeight + "top height");
        return mTopLayout.getHeight();
    }

    private void routeSearch(Poi startPoi, Poi targetPoi, int type) {
        if (startPoi == null || targetPoi == null) {
            return;
        }
        LatLng start = startPoi.getCoordinate();
        LatLng target = targetPoi.getCoordinate();

        RouteSearch routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(this);
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(AMapServicesUtil.convertToLatLonPoint(start), AMapServicesUtil.convertToLatLonPoint(target));
        switch (type) {
            case TYPE_DRIVE:
                RouteSearch.DriveRouteQuery dquery = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST, null, null, "");
                routeSearch.calculateDriveRouteAsyn(dquery);
                break;
            case TYPE_BUS:
                RouteSearch.BusRouteQuery bquery = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT,
                        mAmap.getMyLocation().getExtras().getString(CITY_CODE), 0);
                routeSearch.calculateBusRouteAsyn(bquery);
                break;
            case TYPE_WALK:
                RouteSearch.WalkRouteQuery wquery = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WALK_DEFAULT);
                routeSearch.calculateWalkRouteAsyn(wquery);
                break;
            case TYPE_RIDE:
                RouteSearch.RideRouteQuery rquery = new RouteSearch.RideRouteQuery(fromAndTo, RouteSearch.RIDING_DEFAULT);
                routeSearch.calculateRideRouteAsyn(rquery);
                break;
            default:
                break;
        }
    }


    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
        if (errorCode == 1000) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    updateUiAfterRouted();
                    mDriveRouteResult = driveRouteResult;

                    DrivePath path = mDriveRouteResult.getPaths().get(0);
                    mDrivePathAdapter = new DrivePathAdapter(mActivity, path.getSteps());
                    mPathDetailRecView.setAdapter(mDrivePathAdapter);
                    mPathDetailRecView.setVisibility(View.VISIBLE);

                    mPathTipsText.setText(getString(R.string.route_plan_path_traffic_lights, path.getTotalTrafficlights() + ""));
                    mPathTipsText.setVisibility(View.VISIBLE);
                    drawDriveRoutes(mDriveRouteResult, path);

                    for (int i = 0; i < mDriveRouteResult.getPaths().size(); i++) {
                        updatePathGeneral(mDriveRouteResult.getPaths().get(i), i);
                    }

                    mBehavior.setPeekHeight(getSheetHeadHeight());
                } else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    Toast.makeText(mActivity, R.string.no_result, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mActivity, R.string.no_result, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mActivity, R.string.poi_search_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int errorCode) {
        if (errorCode == 1000) {
            if (busRouteResult != null && busRouteResult.getPaths() != null) {
                if (busRouteResult.getPaths().size() > 0) {
                    if (mTopLayout.getVisibility() != View.VISIBLE) {
//                        mTopSearchLayout.setVisibility(View.GONE);
                        mFloatBtn.hide();
                        ViewAnimUtils.dropDownWithInterpolator(mTopLayout, new ViewAnimUtils.AnimEndListener() {
                            @Override
                            public void onAnimEnd() {
                                mTopLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    mNesteScrollView.setVisibility(View.GONE);

                    mBusRouteResult = busRouteResult;
                    mBusResultAdapter = new BusResultListAdapter(mActivity, busRouteResult);
                    mBusResultRview.setAdapter(mBusResultAdapter);
                    ViewAnimUtils.popupinWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                        @Override
                        public void onAnimEnd() {
                            mBusResultRview.setVisibility(View.VISIBLE);
                        }
                    });
//                    drawBusRoutes(mBusRouteResult,mBusRouteResult.getPaths().get(0));
                } else if (busRouteResult != null && busRouteResult.getPaths() == null) {
                    Toast.makeText(mActivity, R.string.no_result, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mActivity, R.string.no_result, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mActivity, R.string.poi_search_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int errorCode) {
        if (errorCode == 1000) {
            if (walkRouteResult != null && walkRouteResult.getPaths() != null) {
                if (walkRouteResult.getPaths().size() > 0) {
                    updateUiAfterRouted();

                    mWalkRouteResult = walkRouteResult;
                    WalkPath path = mWalkRouteResult.getPaths().get(0);
                    mWalkStepAdapter = new WalkStepAdapter(mActivity, path.getSteps());
                    mPathDetailRecView.setAdapter(mWalkStepAdapter);
                    mPathDetailRecView.setVisibility(View.VISIBLE);
                    drawWalkRoutes(mWalkRouteResult, mWalkRouteResult.getPaths().get(0));

                    for (int i = 0; i < mWalkRouteResult.getPaths().size(); i++) {
                        updatePathGeneral(mWalkRouteResult.getPaths().get(i), i);
                    }

                    mBehavior.setPeekHeight(getSheetHeadHeight());
                } else if (walkRouteResult != null && walkRouteResult.getPaths() == null) {
                    Toast.makeText(mActivity, R.string.no_result, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mActivity, R.string.no_result, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mActivity, R.string.poi_search_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int errorCode) {
        if (errorCode == 1000) {
            if (rideRouteResult != null && rideRouteResult.getPaths() != null) {
                if (rideRouteResult.getPaths().size() > 0) {
                    updateUiAfterRouted();

                    mRideRouteResult = rideRouteResult;
                    RidePath path = mRideRouteResult.getPaths().get(0);

                    mRideStepAdapter = new RideStepAdapter(mActivity, path.getSteps());
                    mPathDetailRecView.setVisibility(View.VISIBLE);
                    drawRideRoutes(mRideRouteResult, mRideRouteResult.getPaths().get(0));

                    for (int i = 0; i < mRideRouteResult.getPaths().size(); i++) {
                        updatePathGeneral(mRideRouteResult.getPaths().get(i), i);
                    }

                    mBehavior.setPeekHeight(getSheetHeadHeight());
                } else if (rideRouteResult != null && rideRouteResult.getPaths() == null) {
                    Toast.makeText(mActivity, R.string.no_result, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mActivity, R.string.no_result, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mActivity, R.string.poi_search_error, Toast.LENGTH_LONG).show();
        }
    }

    private void updateUiAfterRouted() {
        if (mTopLayout.getVisibility() != View.VISIBLE) {
            mFloatBtn.hide();
            ViewAnimUtils.dropDownWithInterpolator(mTopLayout, new ViewAnimUtils.AnimEndListener() {
                @Override
                public void onAnimEnd() {
                    mTopLayout.setVisibility(View.VISIBLE);
                }
            });
        }
        if (mBusResultRview.getVisibility() == View.VISIBLE) {
            ViewAnimUtils.popupoutWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                @Override
                public void onAnimEnd() {
                    mBusResultRview.setVisibility(View.GONE);

                }
            });
        }
        mNaviText.setVisibility(View.VISIBLE);
        mPoiDetailLayout.setVisibility(View.GONE);
        mPathTipsText.setVisibility(View.GONE);
        mNesteScrollView.setVisibility(View.VISIBLE);
    }

    Marker[] markers = new Marker[4];

    private void setLimitAt(List<DriveStep> steps, DriveRouteResult driveRouteResult, DrivePath path, DrivingRouteOverlay drivingRouteOverlay, int index) {
        List<LatLonPoint> polyline = steps.get(index).getPolyline();
        int mI = index - steps.size() / 2 + 1;
        if (polyline != null && polyline.size() > 0) {
            LatLonPoint point = polyline.get(0);
            double limit = Numbers.toDouble(et_limit2.getText().toString(), 2.2);
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
            int pathIndex = driveRouteResult.getPaths().indexOf(path);
            drivingRouteOverlay.setDriveColor(pathIndex == 0 ? 0 : Color.RED);
            Bitmap bitmap = ImageUtil.get(mActivity, pathIndex == 0 ? 5 : 3.5);
            if (markers[mI] != null) {
                markers[mI].remove();
                markers[mI].destroy();
                markers[mI] = null;
            }
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng);
            markers[mI] = mAmap.addMarker(markerOptions);
            markers[mI].setVisible(true);
        }
    }

    private void drawDriveRoutes(DriveRouteResult driveRouteResult, DrivePath path) {
        mAmap.clear();
        final DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                mActivity, mAmap, path,
                driveRouteResult.getStartPos(), driveRouteResult.getTargetPos(),
                null);

        List<DriveStep> steps = path.getSteps();
        int stepSize;
        if (steps != null && (stepSize = steps.size()) > 0) {
            int start = stepSize / 2;
            for (int index = start - 1; index <= start + 1; index++) {
                if (index >= 0 && index < stepSize) {
                    setLimitAt(steps, driveRouteResult, path, drivingRouteOverlay, index);
                }
            }
        }

        drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
        drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
        drivingRouteOverlay.removeFromMap();
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomWithPadding(getTopLayoutHeight(), getSheetHeadHeight());

        mAMapNavi.calculateDriveRoute(
                Collections.singletonList(new NaviLatLng(driveRouteResult.getStartPos().getLatitude(), driveRouteResult.getStartPos().getLongitude()))
                , Collections.singletonList(new NaviLatLng(driveRouteResult.getTargetPos().getLatitude(), driveRouteResult.getTargetPos().getLongitude()))
                , mAMapNavi.strategyConvert(false, false, false, false, false)
        );
    }

    private void drawBusRoutes(BusRouteResult busRouteResult, BusPath path) {
        mAmap.clear();
        BusRouteOverlay busRouteOverlay = new BusRouteOverlay(
                mActivity, mAmap, path, busRouteResult.getStartPos(),
                busRouteResult.getTargetPos());
        busRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
        busRouteOverlay.removeFromMap();
        busRouteOverlay.addToMap();
        busRouteOverlay.zoomWithPadding(getTopLayoutHeight(), getSheetHeadHeight());
    }

    private void drawWalkRoutes(WalkRouteResult walkRouteResult, WalkPath path) {
        mAmap.clear();
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                mActivity, mAmap, path, walkRouteResult.getStartPos(),
                walkRouteResult.getTargetPos());
        walkRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomWithPadding(getTopLayoutHeight(), getSheetHeadHeight());
    }


    private void drawRideRoutes(RideRouteResult rideRouteResult, RidePath path) {
        mAmap.clear();
        RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                mActivity, mAmap, path, rideRouteResult.getStartPos(),
                rideRouteResult.getTargetPos());
        rideRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
        rideRouteOverlay.removeFromMap();
        rideRouteOverlay.addToMap();
        rideRouteOverlay.zoomWithPadding(getTopLayoutHeight(), getSheetHeadHeight());
    }


    @Override
    public void onItemClick(BusPath busPath) {
        drawBusRoutes(mBusRouteResult, busPath);
    }


    @Override
    public void onBackPressed() {
        if (mTopLayout.getVisibility() == View.VISIBLE) {
            ViewAnimUtils.pushOutWithInterpolator(mTopLayout, new ViewAnimUtils.AnimEndListener() {
                @Override
                public void onAnimEnd() {
                    mTopLayout.setVisibility(View.GONE);
                    mAmap.clear();
                    mTopSearchLayout.setVisibility(View.VISIBLE);
                    mNesteScrollView.setVisibility(View.GONE);
                    mPathTipsText.setVisibility(View.GONE);
                    mNaviText.setVisibility(View.GONE);
                    mFloatBtn.hide();
                }
            });
            if (mBusResultRview.getVisibility() == View.VISIBLE) {
                ViewAnimUtils.popupoutWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                    @Override
                    public void onAnimEnd() {
                        mBusResultRview.setVisibility(View.GONE);
                    }
                });
            }
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mMapView.onDestroy();
        mAMapNavi.destroy();
        mSensorManager.unregisterListener(mSensorListner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

}
