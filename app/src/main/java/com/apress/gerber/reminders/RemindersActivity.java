package com.apress.gerber.reminders;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.*;
import android.widget.*;

import java.util.Date;

public class RemindersActivity extends ActionBarActivity {
    private ListView mListView;
    private RemindersDbAdapter mDbAdapter;
    private RemindersSimpleCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        mListView = (ListView) findViewById(R.id.reminders_list_view);
        mListView.setDivider(null);
        mDbAdapter = new RemindersDbAdapter(this);
        mDbAdapter.open();
        Cursor cursor = mDbAdapter.fetchAllReminders();
//from columns defined in the db
        String[] from = new String[]{
                RemindersDbAdapter.COL_CONTENT
        };
//to the ids of views in the layout
        int[] to = new int[]{
                R.id.row_text
        };
        mCursorAdapter = new RemindersSimpleCursorAdapter(
//context
                RemindersActivity.this,
//the layout of the row
                R.layout.reminders_row,
//cursor
                cursor,
//from columns defined in the db
                from,
//to the ids of views in the layout
                to,
//flag - not used
                0);
// the cursorAdapter (controller) is now updating the listView (view)
//with data from the db (model)
        mListView.setAdapter(mCursorAdapter);
//    }
//Abbreviated for brevity


//    private ListView mListView;
//    private RemindersDbAdapter mDbAdapter;
//    private RemindersSimpleCursorAdapter mCursorAdapter;
//
//    @Override
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_reminders);
//        mListView = (ListView) findViewById(R.id.reminders_list_view);
//        mListView.setDivider(null);
//        mDbAdapter = new RemindersDbAdapter(this);
//        mDbAdapter.open();
//        if (savedInstanceState == null) {
////Clear all data
//            //mDbAdapter.deleteAllReminders();
//            //Log.v("删除","妈的全没了！");
////Add some data
//            //insertSomeReminders();
//            //Log.v("已添加","所有备忘");
//                    Cursor cursor = mDbAdapter.fetchAllReminders();
////from columns defined in the db
//        String[] from = new String[]{
//                RemindersDbAdapter.COL_CONTENT
//        };
////to the ids of views in the layout
//        int[] to = new int[]{
//                R.id.row_text
//        };
//        mCursorAdapter = new RemindersSimpleCursorAdapter(
////context
//                RemindersActivity.this,
////the layout of the row
//                R.layout.reminders_row,
////cursor
//                cursor,
////from columns defined in the db
//                from,
////to the ids of views in the layout
//                to,
////flag - not used
//                0);
//// the cursorAdapter (controller) is now updating the listView (view)
////with data from the db (model)
//        mListView.setAdapter(mCursorAdapter);
//        }

        //如果使用新版本API（11）则执行下列代码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Log.v("11API","新版本哦！");
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean
                        checked) { }
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    Log.v("11API","1");
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.cam_menu, menu);
                    return true;
                }
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    Log.v("11API","2");
                    return false;
                }
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    Log.v("11API","3");
                    switch (item.getItemId()) {
                        case R.id.menu_item_delete_reminder:
                            for (int nC = mCursorAdapter.getCount() - 1; nC >= 0; nC--) {
                                if (mListView.isItemChecked(nC)) {
                                    mDbAdapter.deleteReminderById(getIdFromPosition(nC));
                                }
                            }
                            mode.finish();
                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                            return true;
                    }
                    return false;
                }
                @Override
                public void onDestroyActionMode(ActionMode mode) { }
            });

        }
//Removed remaining method code for brevity...

        //when we click an individual item in the listview
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                Log.v("进入","点击条目"+masterListPosition);
                AlertDialog.Builder builder = new AlertDialog.Builder(RemindersActivity.this);
                ListView modeListView = new ListView(RemindersActivity.this);
                String[] modes = new String[] { "Edit Reminder", "Delete Reminder","Schedule Reminder" };
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(RemindersActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        int nId = getIdFromPosition(masterListPosition);
                        final Reminder reminder = mDbAdapter.fetchReminderById(nId);

                        if (position == 0) {

                            fireCustomDialog(reminder);
//delete reminder
                        } else if(position == 1) {
                            mDbAdapter.deleteReminderById(getIdFromPosition(masterListPosition));
                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                        }else{
                            Log.v("时间","提醒");
                            final Date today = new Date();
                            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener(){

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                    Date alarm = new Date(today.getYear(),today.getMonth(),today.getDate(),hourOfDay,minute);

                                    scheduleReminder(alarm.getTime(),reminder.getContent());
                                }//abcd

                            };
                            new TimePickerDialog(RemindersActivity.this,null,today.getMonth(),today.getMinutes(),false).show();


                        }
                        dialog.dismiss();

//edit reminder
//                        if (position == 0) {
//                            Toast.makeText(RemindersActivity.this, "edit "
//                                    + masterListPosition, Toast.LENGTH_SHORT).show();
////delete reminder
//                        } else {
//                            Toast.makeText(RemindersActivity.this, "delete "
//                                    + masterListPosition, Toast.LENGTH_SHORT).show();
//                            //根据条目ID删除数据库中备忘内容
//                            mDbAdapter.deleteReminderById(getIdFromPosition(masterListPosition));
//                            //重新获取显示数据库中条目
//                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
//                        }
//                        dialog.dismiss();
                    }
                });
            }
        });

    }

    private void scheduleReminder(long time, String content) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE); Intent alarmIntent = new Intent(this, ReminderAlarmReceiver.class); alarmIntent.putExtra(ReminderAlarmReceiver.REMINDER_TEXT, content);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, alarmIntent, 0); alarmManager.set(AlarmManager.RTC_WAKEUP, time, broadcast);

    }

    private int getIdFromPosition(int nC) {
        return (int)mCursorAdapter.getItemId(nC);
    }


    private void insertSomeReminders() {
        mDbAdapter.createReminder("Buy Learn Android Studio", true);
        mDbAdapter.createReminder("Send Dad birthday gift", false);
        mDbAdapter.createReminder("Dinner at the Gage on Friday", false);
        mDbAdapter.createReminder("String squash racket", false);
        mDbAdapter.createReminder("Shovel and salt walkways", false);
        mDbAdapter.createReminder("Prepare Advanced Android syllabus", true);
        mDbAdapter.createReminder("Buy new office chair", false);
        mDbAdapter.createReminder("Call Auto-body shop for quote", false);
        mDbAdapter.createReminder("Renew membership to club", false);
        mDbAdapter.createReminder("Buy new Galaxy Android phone", true);
        mDbAdapter.createReminder("Sell old Android phone - auction", false);
        mDbAdapter.createReminder("Buy new paddles for kayaks", false);
        mDbAdapter.createReminder("Call accountant about tax returns", false);
        mDbAdapter.createReminder("Buy 300,000 shares of Google", false);
        mDbAdapter.createReminder("Call the Dalai Lama back", true);
    }
//Removed remaining method code for brevity...brevity


    //------------------------加载溢出菜单 不能动
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
//create new Reminder
                Log.d(getLocalClassName(),"create new Reminder");
                fireCustomDialog(null);
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                return true;
            case R.id.clean_all:
                Log.v("清除","清除所有备忘录");
                mDbAdapter.deleteAllReminders();
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                return true;
            case  R.id.add_all:
                Log.v("添加","添加所有备忘录");
                mDbAdapter.deleteAllReminders();
                insertSomeReminders();
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return false;
        }
    }

    //刷新当前Activity
    private void refresh() {
        finish();
        Intent intent = new Intent(RemindersActivity.this, RemindersActivity.class);
        startActivity(intent);
    }
//        private void insertSomeReminders() {
//        mDbAdapter.createReminder("Buy Learn Android Studio", true);
//        mDbAdapter.createReminder("Send Dad birthday gift", false);
//        mDbAdapter.createReminder("Dinner at the Gage on Friday", false);
//        mDbAdapter.createReminder("String squash racket", false);
//        mDbAdapter.createReminder("Shovel and salt walkways", false);
//        mDbAdapter.createReminder("Prepare Advanced Android syllabus", true);
//        mDbAdapter.createReminder("Buy new office chair", false);
//        mDbAdapter.createReminder("Call Auto-body shop for quote", false);
//        mDbAdapter.createReminder("Renew membership to club", false);
//        mDbAdapter.createReminder("Buy new Galaxy Android phone", true);
//        mDbAdapter.createReminder("Sell old Android phone - auction", false);
//        mDbAdapter.createReminder("Buy new paddles for kayaks", false);
//        mDbAdapter.createReminder("Call accountant about tax returns", false);
//        mDbAdapter.createReminder("Buy 300,000 shares of Google", false);
//        mDbAdapter.createReminder("Call the Dalai Lama back", true);
//    }

    private void fireCustomDialog(final Reminder reminder){
// custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);
        TextView titleView = (TextView) dialog.findViewById(R.id.custom_title);
        final EditText editCustom = (EditText) dialog.findViewById(R.id.custom_edit_reminder);
        Button commitButton = (Button) dialog.findViewById(R.id.custom_button_commit);
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.custom_check_box);
        LinearLayout rootLayout = (LinearLayout) dialog.findViewById(R.id.custom_root_layout);
        final boolean isEditOperation = (reminder != null);
//this is for an edit
        if (isEditOperation){
            titleView.setText("Edit Reminder");
            checkBox.setChecked(reminder.getImportant() == 1);
            editCustom.setText(reminder.getContent());
            rootLayout.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderText = editCustom.getText().toString();
                if (isEditOperation) {
                    Reminder reminderEdited = new Reminder(reminder.getId(),
                            reminderText, checkBox.isChecked() ? 1 : 0);
                    mDbAdapter.updateReminder(reminderEdited);
//this is for new reminder
                } else {
                    mDbAdapter.createReminder(reminderText, checkBox.isChecked());
                }
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                dialog.dismiss();
            }
        });
        Button buttonCancel = (Button) dialog.findViewById(R.id.custom_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
//随便
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflator = new MenuInflater(this);
        // 装载R.menu.my_menu对应的菜单，并添加到menu中
        inflator.inflate(R.menu.menu_reminders, menu);
        return super.onCreateOptionsMenu(menu);
    }

}

//public class RemindersActivity extends ActionBarActivity {

//}


