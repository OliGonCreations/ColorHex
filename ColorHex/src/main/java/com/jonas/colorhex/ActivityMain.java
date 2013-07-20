package com.jonas.colorhex;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jonas.colorhex.color.ColorPicker;
import com.jonas.colorhex.color.OpacityBar;
import com.jonas.colorhex.color.SVBar;
import com.jonas.colorhex.stickylistview.StickyListHeadersAdapter;
import com.jonas.colorhex.stickylistview.StickyListHeadersListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ActivityMain extends FragmentActivity {


    private static DatabaseHandler db;
    private static ClipboardManager clipboard;

    private static SharedPreferences sp;

    public static Resources res;
    public static boolean favIsShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        db = new DatabaseHandler(this);
        res = getResources();
        clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);

        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        if (mPager != null) {
            mPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
            mPager.setPageTransformer(true, new ZoomOutPageTransformer());
            mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i2) {
                }

                @Override
                public void onPageSelected(int i) {
                    if (CardBackFragment.mActionMode != null)
                        CardBackFragment.mActionMode.finish();
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                }
            });
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.leftCard, new CardFrontFragment()).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.rightCard, new CardBackFragment()).commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.action_favorite) {
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new FavoriteFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == android.R.id.home && findViewById(R.id.lvSticky) != null) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            }
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (favIsShown) {
            menu.findItem(R.id.action_favorite).setVisible(false);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            menu.findItem(R.id.action_favorite).setVisible(true);
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public static class FavoriteFragment extends Fragment implements AdapterView.OnItemClickListener {

        private static StickyListHeadersListView mStickyListView;
        private static View mDividerView;
        private static Button mFilter;
        private static TextView mEmptyView;
        private CustomListViewAdapter mAdapter;
        private boolean mIsSorted;
        private SharedPreferences sp;

        public FavoriteFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_favorite, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            sp = getActivity().getSharedPreferences("prefs", 0);
            mIsSorted = sp.getBoolean("mIsSorted", false);
            mStickyListView = (StickyListHeadersListView) view.findViewById(R.id.lvSticky);
            mStickyListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mStickyListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                private int nr = 0;

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.con_action_delete:
                            mAdapter.deleteItem(mAdapter.getCurrentCheckedPosition());
                            mAdapter.clearSelection();
                            nr = 0;
                            mode.finish();
                            break;
                        case R.id.con_action_share:
                            Object[] a = mAdapter.getCurrentCheckedPosition().toArray();
                            Integer[] positions = Arrays.copyOf(a, a.length, Integer[].class);
                            int size = positions.length;
                            String message = getResources().getQuantityString(R.plurals.share_colorcode, size, size);
                            if (size > 1) {
                                for (int i = 0; i < size - 1; i++) {
                                    message += mAdapter.getItem(positions[i]) + ",\u0020";
                                }
                            }
                            message += mAdapter.getItem(positions[size - 1]) + ".";
                            mAdapter.clearSelection();
                            nr = 0;
                            mode.finish();
                            Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_with)));
                            break;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    mAdapter.clearSelection();
                    nr = 0;
                }

                @Override
                public void onItemCheckedStateChanged(ActionMode mode,
                                                      int position, long id, boolean checked) {
                    if (checked) {
                        nr++;
                        mAdapter.setNewSelection(position, checked);
                    } else {
                        nr--;
                        mAdapter.removeSelection(position);
                    }
                    mode.setTitle(nr + getString(R.string.contextual_menu));

                }

            });
            mFilter = (Button) view.findViewById(R.id.btn_filter);
            mDividerView = view.findViewById(R.id.view);
            mEmptyView = (TextView) view.findViewById(R.id.tvEmpty);
            mFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIsSorted = !mIsSorted;
                    sortList();
                }
            });
            sortList();
        }

        @Override
        public void onResume() {
            super.onResume();
            favIsShown = true;
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onPause() {
            super.onPause();
            favIsShown = false;
            getActivity().invalidateOptionsMenu();
            sp.edit().putBoolean("mIsSorted", mIsSorted).commit();
        }

        private void sortList() {
            if (mIsSorted)
                mFilter.setText(getString(R.string.btn_filter_date));
            else
                mFilter.setText(getString(R.string.btn_filter_color));
            int index = mStickyListView.getFirstVisiblePosition();
            View v = mStickyListView.getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();
            mAdapter = new CustomListViewAdapter(getActivity(), db.getAllFavColors(mIsSorted), db.getAllRecColors(mIsSorted), new String[]{res.getString(R.string.list_header_favorite), res.getString(R.string.list_header_recent)}, db.getAllFavValues(1, mIsSorted), db.getAllRecValues(1, mIsSorted));
            if (mAdapter.isEmpty()) {
                listIsEmpty();
            } else {
                mStickyListView.setAdapter(mAdapter);
                mStickyListView.setSelectionFromTop(index, top);
                mStickyListView.setOnItemClickListener(this);
            }
        }

        public static void listIsEmpty() {
            mFilter.setVisibility(View.GONE);
            mDividerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mStickyListView.setItemChecked(i, !mAdapter.isPositionChecked(i));
        }
    }

    public static class CardFrontFragment extends Fragment implements ColorPicker.OnColorChangedListener, CompoundButton.OnCheckedChangeListener {

        private static TextView tvOutputHex, tvOutputDec;
        private static ColorPicker colorPicker;
        private static SVBar svBar;
        private static OpacityBar opacityBar;
        private static CompoundButton star;
        private static Context ctx;
        private static String hexValue;
        private static int opacityValue;
        private static boolean alphaShown;
        private static float[] mHSV = new float[3];

        public CardFrontFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_picker, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ctx = getActivity();
            alphaShown = sp.getBoolean("pref_alpha", false);
            colorPicker = (ColorPicker) view.findViewById(R.id.picker);
            svBar = (SVBar) view.findViewById(R.id.svbar);
            opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
            tvOutputHex = (TextView) view.findViewById(R.id.tvOutputHex);
            tvOutputDec = (TextView) view.findViewById(R.id.tvOutputDec);
            star = (CompoundButton) view.findViewById(R.id.star);
            star.setOnCheckedChangeListener(this);
            CheatSheet.setup(star, "Add to favorite");
            colorPicker.addSVBar(svBar);
            if (alphaShown) {
                opacityBar.setVisibility(View.VISIBLE);
                colorPicker.addOpacityBar(opacityBar);
            }
            colorPicker.setOnColorChangedListener(this);
            int color = sp.getInt("color", getResources().getColor(R.color.ab_red));
            colorPicker.setColor(color);
            colorPicker.setNewCenterColor(color);
            setColor(color);
        }

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            super.onSaveInstanceState(savedInstanceState);
            sp.edit().putInt("color", colorPicker.getColor()).commit();
        }

        public static void setColor(int color) {
            colorPicker.setOldCenterColor(color);
            hexValue = String.format("#%06X", (0xFFFFFF & colorPicker.getColor()));
            opacityValue = opacityBar.getOpacity();
            if (alphaShown) {
                tvOutputDec.setText("A:" + opacityValue + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")");
                hexValue = "A:" + Integer.toHexString(opacityValue).toUpperCase() + " " + hexValue;
                tvOutputHex.setText(hexValue);
            } else {
                tvOutputHex.setText(hexValue);
                tvOutputDec.setText("(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")");
            }

        }

        @Override
        public void onColorChanged(int color) {
            setColor(color);
            star.setChecked(false);
        }

        public static void onCenterPressed() {
            hexValue = String.format("#%06X", (0xFFFFFF & colorPicker.getColor()));
            String text = "";
            switch (Integer.parseInt(sp.getString(SettingsActivity.pref_clipboard, "0"))) {
                case 0:
                    text = hexValue;
                    break;
                case 1:
                    text = "A:" + Integer.toHexString(opacityValue).toUpperCase() + " " + hexValue;
                    break;
                case 2:
                    text = "(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")";
                    break;
                case 3:
                    text = "A:" + opacityValue + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")";
                    break;

            }
            ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
            Color.colorToHSV(colorPicker.getColor(), mHSV);
            String format = "%1$03d";
            String mString = String.format(format, (int) mHSV[0]) + String.format(format, (int) mHSV[1]) + String.format(format, (int) mHSV[2]);
            Toast.makeText(ctx, res.getString(R.string.clipboard), Toast.LENGTH_SHORT).show();
            db.addRecent(colorPicker.getColor(), mString, hexValue, "A:" + Integer.toHexString(opacityValue).toUpperCase() + " " + hexValue, "(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")", "A:" + opacityValue + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")");
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (compoundButton.getId() == R.id.star) {
                if (b) {
                    Color.colorToHSV(colorPicker.getColor(), mHSV);
                    String format = "%1$03d";
                    String mString = String.format(format, (int) mHSV[0]) + String.format(format, (int) mHSV[1]) + String.format(format, (int) mHSV[2]);
                    hexValue = String.format("#%06X", (0xFFFFFF & colorPicker.getColor()));
                    db.addFavorite(colorPicker.getColor(), mString, hexValue, "A:" + Integer.toHexString(opacityValue).toUpperCase() + " " + hexValue, "(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")", "A:" + opacityValue + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")");
                }
            }
        }

    }

    public static class CardBackFragment extends Fragment implements View.OnClickListener {

        private ImageView color1, color2, color3, color4, color5;
        public static EditText etColor2, etColor3;
        public static ExtendedEditText etColor1, etColor4, etColor5;
        private Button btClear1, btClear2, btClear3, btClear4, btClear5, btAdd, btRemove;
        public static ActionMode mActionMode;
        private int selectedColor = 0, mCount = 0;
        private boolean isJelly = false;

        public CardBackFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_setter, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    if (end > start) {

                        char[] acceptedChars = new char[]{'a', 'b', 'c', 'd', 'e', 'f',
                                'A', 'B', 'C', 'D', 'E', 'F',
                                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                ','};

                        for (int index = start; index < end; index++) {
                            if (!new String(acceptedChars).contains(String.valueOf(source.charAt(index)))) {
                                return "";
                            }
                        }
                    }
                    return null;
                }

            };
            isJelly = android.os.Build.VERSION.SDK_INT >= 17;
            color1 = (ImageView) view.findViewById(R.id.color1);
            color2 = (ImageView) view.findViewById(R.id.color2);
            color3 = (ImageView) view.findViewById(R.id.color3);
            color4 = (ImageView) view.findViewById(R.id.color4);
            color5 = (ImageView) view.findViewById(R.id.color5);
            etColor1 = (ExtendedEditText) view.findViewById(R.id.etColor1);
            etColor2 = (EditText) view.findViewById(R.id.etColor2);
            etColor3 = (EditText) view.findViewById(R.id.etColor3);
            etColor4 = (ExtendedEditText) view.findViewById(R.id.etColor4);
            etColor5 = (ExtendedEditText) view.findViewById(R.id.etColor5);
            etColor1.setFilters(filters);
            etColor4.setFilters(filters);
            etColor5.setFilters(filters);
            btClear1 = (Button) view.findViewById(R.id.btClear1);
            btClear2 = (Button) view.findViewById(R.id.btClear2);
            btClear3 = (Button) view.findViewById(R.id.btClear3);
            btClear4 = (Button) view.findViewById(R.id.btClear4);
            btClear5 = (Button) view.findViewById(R.id.btClear5);
            if (isJelly) {
                etColor1.setPrefix("#");
                etColor4.setPrefix("#");
                etColor5.setPrefix("#");
                etColor1.setText(sp.getString(etColor1.getId() + "", ""));
                etColor4.setText(sp.getString(etColor4.getId() + "", ""));
                etColor5.setText(sp.getString(etColor5.getId() + "", ""));
            } else {
                etColor1.setText(sp.getString("#" + etColor1.getId(), "#"));
                etColor4.setText(sp.getString(etColor4.getId() + "", "#"));
                etColor5.setText(sp.getString(etColor5.getId() + "", "#"));
            }
            etColor2.setText(sp.getString(etColor2.getId() + "", "#"));
            etColor3.setText(sp.getString(etColor3.getId() + "", "#"));
            btClear1.setOnClickListener(this);
            btClear2.setOnClickListener(this);
            btClear3.setOnClickListener(this);
            btClear4.setOnClickListener(this);
            btClear5.setOnClickListener(this);
            color1.setOnClickListener(this);
            color2.setOnClickListener(this);
            color3.setOnClickListener(this);
            color4.setOnClickListener(this);
            color5.setOnClickListener(this);
            saveText(etColor1, color1);
            saveText(etColor2, color2);
            saveText(etColor3, color3);
            saveText(etColor4, color4);
            saveText(etColor5, color5);
            etColor1.addTextChangedListener(textWatcher(etColor1, color1));
            etColor2.addTextChangedListener(textWatcher(etColor2, color2));
            etColor3.addTextChangedListener(textWatcher(etColor3, color3));
            etColor4.addTextChangedListener(textWatcher(etColor4, color4));
            etColor5.addTextChangedListener(textWatcher(etColor5, color5));
            btAdd = (Button) view.findViewById(R.id.btAdd);
            btRemove = (Button) view.findViewById(R.id.btRemove);
            btAdd.setOnClickListener(this);
            btRemove.setOnClickListener(this);
        }

        TextWatcher textWatcher(final EditText et, final ImageView img) {
            return new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveText(et, img);
                }
            };
        }

        private void saveText(EditText et, ImageView img) {
            try {
                String s = et.getText().toString();
                int color;
                if (!s.contains("#"))
                    s = "#" + s;
                if (s.contains(",")) {
                    String[] a = s.split(",");
                    a[0] = a[0].replace("#", "");
                    s = "#" + Integer.toHexString(Integer.parseInt(a[0]))
                            + Integer.toHexString(Integer.parseInt(a[1]))
                            + Integer.toHexString(Integer.parseInt(a[2]));
                }
                color = Color.parseColor(s);
                img.setBackgroundColor(color);
            } catch (Exception e) {
                img.setBackgroundColor(Color.WHITE);
            }
            sp.edit().putString(et.getId() + "", et.getText().toString()).commit();
        }


        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btClear1:
                    if (!isJelly)
                        etColor1.setText("#");
                    else
                        etColor1.setText("");
                    saveText(etColor1, color1);
                    break;
                case R.id.btClear2:
                    etColor2.setText("#");
                    saveText(etColor2, color2);
                    break;
                case R.id.btClear3:
                    etColor3.setText("#");
                    saveText(etColor3, color3);
                    break;
                case R.id.btClear4:
                    if (!isJelly)
                        etColor4.setText("#");
                    else
                        etColor4.setText("");
                    saveText(etColor4, color4);
                    break;
                case R.id.btClear5:
                    if (!isJelly)
                        etColor5.setText("#");
                    else
                        etColor5.setText("");
                    saveText(etColor5, color5);
                    break;
                case R.id.btAdd:
                    mCount++;
                    if (mCount > 0) btRemove.setVisibility(View.VISIBLE);
                    else btRemove.setVisibility(View.GONE);
                    if (mCount > 0) {
                        etColor4.setVisibility(View.VISIBLE);
                        color4.setVisibility(View.VISIBLE);
                        btClear4.setVisibility(View.VISIBLE);
                    }
                    if (mCount > 1) {
                        etColor5.setVisibility(View.VISIBLE);
                        color5.setVisibility(View.VISIBLE);
                        btClear5.setVisibility(View.VISIBLE);
                        btAdd.setVisibility(View.GONE);
                    }
                    break;
                case R.id.btRemove:
                    mCount--;
                    if (mCount > 0) btRemove.setVisibility(View.VISIBLE);
                    else btRemove.setVisibility(View.GONE);
                    if (mCount < 1) {
                        etColor4.setVisibility(View.GONE);
                        color4.setVisibility(View.GONE);
                        btClear4.setVisibility(View.GONE);
                    } else if (mCount < 2) {
                        etColor5.setVisibility(View.GONE);
                        color5.setVisibility(View.GONE);
                        btClear5.setVisibility(View.GONE);
                        btAdd.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.color1:
                case R.id.color2:
                case R.id.color3:
                case R.id.color4:
                case R.id.color5:
                    if (mActionMode != null) {
                        break;
                    }
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    selectedColor = view.getId();
                    break;
            }
        }

        private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.context_menu_color, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                String text = "";

                switch (selectedColor) {
                    case R.id.color1:
                        text = etColor1.getText().toString();
                        break;
                    case R.id.color2:
                        text = etColor2.getText().toString();
                        break;
                    case R.id.color3:
                        text = etColor3.getText().toString();
                        break;
                    case R.id.color4:
                        text = etColor4.getText().toString();
                        break;
                    case R.id.color5:
                        text = etColor5.getText().toString();
                        break;
                }
                switch (item.getItemId()) {
                    case R.id.con_action_clipboard:
                        ClipData clip = android.content.ClipData.newPlainText("Copied Color", text);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), getString(R.string.clipboard), Toast.LENGTH_SHORT).show();
                        selectedColor = 0;
                        mode.finish();
                        return true;
                    case R.id.con_action_fav:
                        if (!text.contains("#"))
                            text = "#" + text;
                        try {
                            if (text.contains(",")) {
                                String[] a = text.split(",");
                                a[0] = a[0].replace("#", "");
                                text = "#" + Integer.toHexString(Integer.parseInt(a[0]))
                                        + Integer.toHexString(Integer.parseInt(a[1]))
                                        + Integer.toHexString(Integer.parseInt(a[2]));
                            }
                            int color = Color.parseColor(text);
                            float[] mHSV = new float[3];
                            Color.colorToHSV(color, mHSV);
                            String format = "%1$03d";
                            String mString = String.format(format, (int) mHSV[0]) + String.format(format, (int) mHSV[1]) + String.format(format, (int) mHSV[2]);
                            String hexValue = String.format("#%06X", (0xFFFFFF & color));
                            db.addFavorite(color, mString, hexValue, "A:" + Integer.toHexString(Color.alpha(color)).toUpperCase() + " " + hexValue, "(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")", "A:" + Color.alpha(color) + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")");
                            Toast.makeText(getActivity(), getString(R.string.saved_to_favorite), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.d("ColorHex", e.toString());
                            Toast.makeText(getActivity(), getString(R.string.error_no_color), Toast.LENGTH_SHORT).show();
                        }
                        selectedColor = 0;
                        mode.finish();
                        return true;
                    case R.id.con_action_edit:
                        if (!text.contains("#"))
                            text = "#" + text;
                        try {
                            int color = Color.parseColor(text);
                            CardFrontFragment.setColor(color);
                            CardFrontFragment.colorPicker.setColor(color);
                            CardFrontFragment.colorPicker.setNewCenterColor(color);
                            CardFrontFragment.colorPicker.changeValueBarColor(color);
                        } catch (Exception e) {
                            Log.e("ColorHex", e.toString());
                        }
                        selectedColor = 0;
                        mode.finish();
                        return true;
                    case R.id.con_action_share:
                        String message = "";
                        Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_with)));
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
            }
        };
    }

    public static class CustomListViewAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        private LayoutInflater inflater;
        private List<String> codeFav, codeRec;
        private List<Integer> colorsRec, colorsFav;
        private String[] headers;
        private DatabaseHandler db;

        private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

        public CustomListViewAdapter(Context context, List<Integer> colorsFav, List<Integer> colorsRec, String[] headers, List<String> codeFav, List<String> codeRec) {
            inflater = LayoutInflater.from(context);
            this.colorsFav = colorsFav;
            this.colorsRec = colorsRec;
            this.headers = headers;
            this.codeFav = codeFav;
            this.codeRec = codeRec;
            db = new DatabaseHandler(context);
        }

        public boolean isEmpty() {
            return colorsFav.size() == 0 && colorsRec.size() == 0;
        }

        public void setNewSelection(int position, boolean value) {
            mSelection.put(position, value);
            notifyDataSetChanged();
        }

        public boolean isPositionChecked(int position) {
            Boolean result = mSelection.get(position);
            return result == null ? false : result;
        }

        public Set<Integer> getCurrentCheckedPosition() {
            return mSelection.keySet();
        }

        public void removeSelection(int position) {
            mSelection.remove(position);
            notifyDataSetChanged();
        }

        public void clearSelection() {
            mSelection = new HashMap<Integer, Boolean>();
            notifyDataSetChanged();
        }

        public void deleteItem(Set<Integer> positions) {
            List<Integer> indices = new ArrayList<Integer>(positions);
            Collections.sort(indices, Collections.reverseOrder());
            int index;
            for (int pos : indices) {
                if (colorsFav == null) index = 0;
                else index = colorsFav.size();
                if (pos >= index) {
                    db.deleteRecent(colorsRec.get(pos - index));
                    colorsRec.remove(pos - index);
                    codeRec.remove(pos - index);
                } else {
                    db.deleteFavorite(colorsFav.get(pos));
                    colorsFav.remove(pos);
                    codeFav.remove(pos);
                }
            }
            notifyDataSetChanged();
            if (isEmpty()) {
                FavoriteFragment.listIsEmpty();
            }
        }

        @Override
        public int getCount() {
            return (colorsFav.size() + colorsRec.size());
        }

        @Override
        public String getItem(int position) {
            int index;
            if (codeFav == null) index = 0;
            else index = codeFav.size();
            if (position >= index)
                return codeRec.get(position - index);
            else
                return codeFav.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.tvItem);
                holder.colorView = (ImageView) convertView.findViewById(R.id.item_color);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            int index;
            if (colorsFav == null) index = 0;
            else index = colorsFav.size();
            if (position >= index) {
                holder.colorView.setBackgroundColor(colorsRec.get(position - index));
                holder.text.setText(codeRec.get(position - index));
            } else {
                holder.colorView.setBackgroundColor(colorsFav.get(position));
                holder.text.setText(codeFav.get(position));
            }

            if (mSelection.get(position) != null) {
                convertView.setActivated(true);
            } else {
                convertView.setActivated(false);
            }
            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.list_header, parent, false);
                holder.text1 = (TextView) convertView.findViewById(R.id.header);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            if (colorsFav.size() == 0 || position >= colorsFav.size())
                holder.text1.setText(headers[1]);
            else holder.text1.setText(headers[0]);
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            if (position >= colorsFav.size())
                return 2L;
            else return 1L;
        }

        class HeaderViewHolder {
            TextView text1;
        }

        class ViewHolder {
            TextView text;
            ImageView colorView;
        }

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CardFrontFragment();
                case 1:
                    return new CardBackFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private float MIN_SCALE = 0.85f;
        private float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
                float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horizontalMargin - verticalMargin / 2);
                } else {
                    view.setTranslationX(-horizontalMargin + verticalMargin / 2);
                }

                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else {
                view.setAlpha(0);
            }
        }
    }

}
