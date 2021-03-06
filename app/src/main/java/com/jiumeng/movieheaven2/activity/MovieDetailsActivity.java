package com.jiumeng.movieheaven2.activity;

import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jiumeng.movieheaven2.R;
import com.jiumeng.movieheaven2.comment.CommentsView;
import com.jiumeng.movieheaven2.comment.OnCommentClickListener;
import com.jiumeng.movieheaven2.engine.DownloadManager;
import com.jiumeng.movieheaven2.engine.GlideRoundTransform;
import com.jiumeng.movieheaven2.engine.AccountManager;
import com.jiumeng.movieheaven2.entity.CommentEntity;
import com.jiumeng.movieheaven2.entity.MovieEntity;
import com.jiumeng.movieheaven2.network.MyStringCallback;
import com.jiumeng.movieheaven2.network.NetWorkApi;
import com.jiumeng.movieheaven2.provider.ProcessData;
import com.jiumeng.movieheaven2.utils.MyTextUtils;
import com.jiumeng.movieheaven2.utils.UIUtils;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.Call;

/**
 * Created by jiumeng on 2016/10/27.
 */
public class MovieDetailsActivity extends BaseActivity {


    @BindView(R.id.iv_img)
    ImageView ivImg;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_years)
    TextView tvYears;
    @BindView(R.id.tv_subtitle)
    TextView tvSubtitle;
    @BindView(R.id.tv_play_time)
    TextView tvPlayTime;
    @BindView(R.id.tv_language)
    TextView tvLanguage;
    @BindView(R.id.tv_category)
    TextView tvCategory;
    @BindView(R.id.tv_ibdm)
    TextView tvIbdm;
    @BindView(R.id.tv_update_time)
    TextView tvUpdateTime;
    @BindView(R.id.tv_file_size)
    TextView tvFileSize;
    @BindView(R.id.ll_file_size)
    LinearLayout llFileSize;
    @BindView(R.id.tv_country)
    TextView tvCountry;
    @BindView(R.id.tv_director)
    TextView tvDirector;
    @BindView(R.id.tv_starring)
    TextView tvStarring;
    @BindView(R.id.tv_introduction)
    TextView tvIntroduction;
    @BindView(R.id.iv_img2)
    ImageView ivImg2;
    @BindView(R.id.iv_fav)
    ImageView ivFav;
    @BindView(R.id.iv_share)
    ImageView ivShare;
    @BindView(R.id.iv_download)
    ImageView ivDownload;
    @BindView(R.id.lay_detail_comment)
    CommentsView layDetailComment;
    @BindView(R.id.et_comment)
    EditText etComment;
    @BindView(R.id.picture)
    ImageView picture;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private MovieEntity mMovieDetail;

    /**
     * Extra - Item.
     */
    public static final String EXTRA_MOVIE = "item";
    public static final String EXTRA_NEED_LOAD_DETAIL = "needLoadDetail";
    private boolean needLoadDetail;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_movie_detail;
    }

    @Override
    protected void initViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        etComment.clearFocus();
    }

    @Override
    protected void initData() {
        mMovieDetail = (MovieEntity) getIntent().getSerializableExtra(EXTRA_MOVIE);
        needLoadDetail = getIntent().getBooleanExtra(EXTRA_NEED_LOAD_DETAIL, false);
        getSupportActionBar().setTitle(mMovieDetail.minName);
        initUI();
    }


    private void initUI() {
        if (!needLoadDetail) {
            setPageContent();
        } else {
            UIUtils.showToast("正在加载");
            loadMovieDetail();
        }
        loadComment();
    }

    /**
     * 显示电影详情界面
     */
    private void loadMovieDetail() {
        NetWorkApi.getMovieDetailInfo(mMovieDetail.url, this, new MyStringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                //网络
                UIUtils.showToast(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                mMovieDetail = ProcessData.parseMovieDetails(response, mMovieDetail, false);
                if (mMovieDetail != null) {
                    setPageContent();
                } else {
                    UIUtils.showToast("加载失败");
                }

            }
        });

    }

    private void setPageContent() {
        String imgUrl = mMovieDetail.jpgList.get(0);
        if (imgUrl != null) {
            Glide.with(this).load(imgUrl).error(R.drawable.default_movie_image).bitmapTransform(new BlurTransformation(this, 50)).into(picture);
            Glide.with(this).load(imgUrl).error(R.drawable.default_movie_image).bitmapTransform(new GlideRoundTransform(this, 4)).into(ivImg);
        }
        if (mMovieDetail.jpgList != null && mMovieDetail.jpgList.size() > 1) {
            Glide.with(this).load(mMovieDetail.jpgList.get(1)).error(R.drawable.default_movie_image2).into(ivImg2);
        }
        tvTitle.setText(mMovieDetail.minName);
        tvYears.setText("年代：" + mMovieDetail.years);
        tvSubtitle.setText("字幕：" + mMovieDetail.subtitle);
        tvPlayTime.setText("片长：" + mMovieDetail.play_time);
        tvLanguage.setText("语言：" + mMovieDetail.language);
        tvCategory.setText("类别：" + mMovieDetail.category);
        tvIbdm.setText("评分：" + mMovieDetail.grade);
        tvUpdateTime.setText("更新日期：" + mMovieDetail.updatetime);
        if (MyTextUtils.isEmpty(mMovieDetail.filesize)) {
            llFileSize.setVisibility(View.GONE);
        } else {
            tvFileSize.setText("文件大小：" + mMovieDetail.filesize);
        }
        tvCountry.setText("国家：　" + mMovieDetail.country);
        tvDirector.setText(mMovieDetail.director);
        tvStarring.setText(mMovieDetail.starring);
        tvIntroduction.setText("  " + mMovieDetail.introduction);
    }

    private void loadComment() {
        layDetailComment.initComment(mMovieDetail.minName);
        etComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String content = etComment.getText().toString();
                    etComment.setText("");
                    final CommentEntity comment = new CommentEntity();
                    comment.setAuthor("jiumeng");
                    comment.setContent(content);
                    comment.setMovieName(mMovieDetail.minName);
                    comment.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                UIUtils.showToast("评论成功");
                                layDetailComment.setFocusable(true);
                                layDetailComment.scrollTo(0, layDetailComment.getScrollY());
                                layDetailComment.addComment(false, comment, new OnCommentClickListener() {
                                    @Override
                                    public void onClick(View view, CommentEntity comment) {
                                        UIUtils.showToast("暂未实现");
                                    }
                                });
                            } else {
                                UIUtils.showToast(e.getLocalizedMessage());
                            }
                        }
                    });
                }
                return false;
            }
        });
    }


    @OnClick({R.id.iv_fav, R.id.iv_share, R.id.iv_download})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_fav:
                AccountManager.getInstance().addFavorite(this, mMovieDetail, new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            UIUtils.showToast("收藏成功" + s);
                        } else {
                            UIUtils.showToast(e.getLocalizedMessage());
                        }
                    }
                });
                break;
            case R.id.iv_share:
                showShare();
                break;
            case R.id.iv_download:
                new DownloadManager(this).startXunlei(mMovieDetail.downlist.get(0));
                break;
        }
    }


    private void showShare() {
        String downUrl = "";
        if (mMovieDetail.downlist != null && mMovieDetail.downlist.size() > 0) {
            downUrl = mMovieDetail.downlist.get(0);
        }

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        oks.setTitle("电影天堂");
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(NetWorkApi.APK_DOWNLOAD);
        // text是分享文本，所有平台都需要这个字段
        oks.setText("这部电影真的不错喔：" + mMovieDetail.minName + "\n下载链接：" + downUrl);

//        oks.setUrl(downUrl);


        //设置图片链接
        oks.setImageUrl(mMovieDetail.jpgList.get(0));
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("电影天堂");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("电影天堂");

        // 启动分享GUI
        oks.show(UIUtils.getContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return (super.onOptionsItemSelected(item));
    }

}
