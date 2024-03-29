package socialtoilet.android.activities;

import java.util.Collection;
import java.util.UUID;

import socialtoilet.android.R;
import socialtoilet.android.activities.dialogs.CalificationDialogFragment;
import socialtoilet.android.activities.dialogs.CalificationDialogFragment.ICalificationDialogDataSource;
import socialtoilet.android.activities.dialogs.CalificationDialogFragment.ICalificationDialogDelegate;
import socialtoilet.android.model.IRating;
import socialtoilet.android.model.IToilet;
import socialtoilet.android.model.IToiletTrait;
import socialtoilet.android.services.factories.ServicesFactory;
import socialtoilet.android.services.get.IRetrieveToiletRatingService;
import socialtoilet.android.services.get.IRetrieveToiletRatingServiceDelegate;
import socialtoilet.android.services.get.IRetrieveToiletTraitsService;
import socialtoilet.android.services.get.IRetrieveToiletTraitsServiceDelegate;
import socialtoilet.android.services.get.IRetrieveToiletUserQualificationService;
import socialtoilet.android.services.get.IRetrieveToiletUserQualificationServiceDelegate;
import socialtoilet.android.services.post.IQualificateToiletService;
import socialtoilet.android.services.post.IQualificateToiletServiceDelegate;
import socialtoilet.android.services.put.IEditQualificationToiletService;
import socialtoilet.android.services.put.IEditQualificationToiletServiceDelegate;
import socialtoilet.android.utils.Settings;
import socialtoilet.android.utils.StateSaver;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.content.Intent;
import android.graphics.Color;

public class DetailToiletActivity extends FragmentActivity
	implements ICalificationDialogDataSource,
	ICalificationDialogDelegate, IQualificateToiletServiceDelegate,
	IRetrieveToiletRatingServiceDelegate, 
	IRetrieveToiletUserQualificationServiceDelegate, 
	IEditQualificationToiletServiceDelegate, IRetrieveToiletTraitsServiceDelegate
{

	public final static String KEY_UUID_OBJECT_RETRIEVER = "kToiletStream";
	public static final String EXTRA_TOILET_ID = "socialtoilet.andorid.detailtoilet.TOILET";
	private IToilet toilet;
	private Collection<IToiletTrait> traits;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_toilet);
		
	    if(null == savedInstanceState)
	    {
			toilet = StateSaver.getInstance().retrieveToilet(MappingToiletActivity.EXTRA_TOILET_ID);
			if(null != toilet)
			{
				populateData();
				
				IRetrieveToiletRatingService service = ServicesFactory.createRetrieveToiletRatingService();
				service.retrieveToiletRating(toilet.getID().toString(), this);
				IRetrieveToiletUserQualificationService calificationService = 
						ServicesFactory.createRetrieveToiletUserCalificationService();
				calificationService.retrieveToiletUserCalification(this, toilet.getID().toString());
			
				IRetrieveToiletTraitsService ttService = 
						ServicesFactory.createRetrieveToiletTraitsService();
				ttService.retrieveToiletTraitService(this, toilet);
			}
			else
			{
				// TODO onToiletNull
			}
	    }
	    else
	    {
		    String objectUUID = savedInstanceState.getString(KEY_UUID_OBJECT_RETRIEVER);
		    UUID objectToRetrieveUUID = UUID.fromString(objectUUID);
		    toilet = StateSaver.getInstance().retrieveToilet(objectToRetrieveUUID.toString());
			this.populateData();
	    }
	    
	    final RatingBar ratingBar = (RatingBar) findViewById(R.id.userCalification);
	    ratingBar.setIsIndicator(true);
	    ratingBar.setOnTouchListener(new OnTouchListener()
	    {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1)
			{
				onCalificationButtonTapped(ratingBar);
				return false;
			}
	    });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.detail_toilet, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_exit:
			onCloseButtonTapped();
	    	return true;
		case R.id.item_comment_button:
			onCommentsButtonTapped();
	    	return true;
		case R.id.item_gallery_button:
			onGaleryButtonTapped();
	    	return true;
		case R.id.action_settings:
	        startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle icicle)
	{
		super.onSaveInstanceState(icicle);
		UUID id = UUID.randomUUID();
		StateSaver.getInstance().saveToilet(id.toString(), toilet);
		icicle.putString(KEY_UUID_OBJECT_RETRIEVER, id.toString());
	}
	
	private void populateData()
	{
		TextView title = (TextView) findViewById(R.id.toiletDescription);
		title.setText(toilet.getDescription());

		TextView address = (TextView) findViewById(R.id.toiletAddress);
		address.setText(toilet.getAddress());
	}

	private void populateGlobalRating()
	{
		RatingBar ratingBar = (RatingBar) findViewById(R.id.globalRating);
		ratingBar.setRating(toilet.getRanking());
		TextView calificationsCount = (TextView) findViewById(R.id.calificationCount);
		calificationsCount.setText(toilet.getUserCalificationsCount() + "");
	}
	
	private void populateUserCalification()
	{
		RatingBar userCalificationBar = (RatingBar) findViewById(R.id.userCalification);
		userCalificationBar.setRating(toilet.getUserCalification());
	}
	
    public void onCalificationButtonTapped(View view)
    {
    	CalificationDialogFragment dialog = new CalificationDialogFragment();
    	dialog.show(getSupportFragmentManager(), "calificate");
    }
	
    private void onCloseButtonTapped()
    {
    	Settings.getInstance().logoutUser();
     	Intent intent = new Intent(this, StartSessionActivity.class);
     	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);	
	}
    
    public void onCommentsButtonTapped()
    {
    	Intent intent = new Intent(this, ToiletCommentsActivity.class);
		intent.putExtra(DetailToiletActivity.EXTRA_TOILET_ID, toilet.getID().toString());
    	startActivity(intent);
    }

    public void onGaleryButtonTapped()
    {
    	Intent intent = new Intent(this, ToiletGaleryActivity.class);
		intent.putExtra(DetailToiletActivity.EXTRA_TOILET_ID, toilet.getID().toString());
    	startActivity(intent);
    }

	private void populateTraits()
	{
		LinearLayout container = (LinearLayout) findViewById(R.id.checkboxLayout);
		container.removeAllViews();
		for(final IToiletTrait trait : traits)
		{
			final CheckBox cb = new CheckBox(this);
			cb.setText(trait.getDescription());
			cb.setTextColor(Color.WHITE);
			cb.setButtonDrawable(getResources().getDrawable(R.drawable.st_checkbox));
			cb.setChecked(trait.hasDescription());
			cb.setClickable(false);
			container.addView(cb);
		}
	}

	@Override
	public void onDialogCalificateClick(CalificationDialogFragment dialog)
	{
		int userCalification = dialog.getUserCalification();
		if(0 == toilet.getUserCalification())
		{
			toilet.setUserCalification(userCalification);
			populateGlobalRating();
			populateUserCalification();
			IQualificateToiletService calificate = ServicesFactory.createQualificateToiletService();
			calificate.qualificateToiletService(toilet, userCalification, this);
		}
		else
		{
			IEditQualificationToiletService edit = ServicesFactory.createEditCalificationToiletService();
			edit.editQualificationToiletService(toilet, userCalification, this);
		}
	}

	@Override
	public void onDialogCancelClick(CalificationDialogFragment dialog) { }

	@Override
	public float getUserCalification()
	{
		return toilet.getUserCalification();
	}

	@Override
	public void qualificateToiletFinish(IQualificateToiletService service) { }

	@Override
	public void qualificateToiletFinishWithError(
			IQualificateToiletService service, int errorCode)
	{
		toilet.revertUserCalification();
		populateGlobalRating();
		populateUserCalification();
	}

	@Override
	public void retrieveToiletRatingServiceFinish(
			IRetrieveToiletRatingService service, IRating rating)
	{
		toilet.setRating(rating);
		populateGlobalRating();
	}

	@Override
	public void retrieveToiletRatingServiceFinishWithError(
			IRetrieveToiletRatingService service, String errorCode)
	{
	}

	@Override
	public void retrieveToiletUserQualificationServiceFinish(
			IRetrieveToiletUserQualificationService service, double calification)
	{
		toilet.userCalificationRetrieved((int)calification);
		populateGlobalRating();
		populateUserCalification();
	}


	@Override
	public void retrieveToiletUserQualificationServiceFinishWithError(
			IRetrieveToiletUserQualificationService service, int errorCode)
	{ 
		// no hacer naaaa'
	}

	@Override
	public void editQualificationToiletFinish(
			IEditQualificationToiletService service, int newQualification)
	{
		toilet.setUserCalification(newQualification);
		populateGlobalRating();
		populateUserCalification();
	}

	@Override
	public void editQualificationToiletFinishWithError(
			IEditQualificationToiletService service, int errorCode)
	{ 
		// TODO mostrar ErrorDialog diciendo que no se pudo recalificar
	}

	@Override
	public void retrieveToiletTraitsServiceFinish(
			IRetrieveToiletTraitsService service,
			Collection<IToiletTrait> traits)
	{
		this.traits = traits;
		populateTraits();
	}

	@Override
	public void retrieveToiletTraitsServiceFinishWithError(
			IRetrieveToiletTraitsService service, int errorCode)
	{
		// TODO Auto-generated method stub
		
	}
}
