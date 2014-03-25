package de.schildbach.wallet.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
 
// Code from: http://lukencode.com/2010/04/27/calling-web-services-in-android-using-httpclient/
public class RestClient
{
    private static final Object BASE_URL = "https://arigato-bitcoin.appspot.com/";
    private ArrayList<NameValuePair> params;
    private ArrayList<NameValuePair> headers;
    private ArrayList<String> part_name;
    private String body;
 
    private String url;
 
    private int responseCode;
    private String message;
 
    private String response;
 
    public String getResponse()
    {
        return response;
    }
 
    public String getErrorMessage()
    {
        return message;
    }
 
    public int getResponseCode()
    {
        return responseCode;
    }
 
    public RestClient(String path) {
        this.url = BASE_URL + path;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
//        part_byte = new  ArrayList<ByteArrayBody>();
        part_name = new  ArrayList<String>();
    }

    public String getUrl(){
    	return url;
    }
    public RestClient AddParam(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
        return this;
    }
 
    public RestClient AddHeader(String name, String value)
    {
        headers.add(new BasicNameValuePair(name, value));
        return this;
    }
    
    public RestClient AddBody(String s){
    	body = s;
        return this;
    }
 
    public String Execute(RequestMethod method) throws Exception
    {
        switch (method)
        {
        case GET:
        {
            // add parameters
            String combinedParams = "";
            if (!params.isEmpty())
            {
                combinedParams += "?";
                for (NameValuePair p : params)
                {
                    String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
                    if (combinedParams.length() > 1)
                    {
                        combinedParams += "&" + paramString;
                    }
                    else
                    {
                        combinedParams += paramString;
                    }
                }
            }
 
            HttpGet request = new HttpGet(url + combinedParams);
 
            // add headers
            for (NameValuePair h : headers)
            {
                request.addHeader(h.getName(), h.getValue());
            }
 
            return executeRequest(request, url);
        }
        case POST:
        {
            HttpPost request = new HttpPost(url);
 
            // add headers
            for (NameValuePair h : headers)
            {
                request.addHeader(h.getName(), h.getValue());
            }
 
            if (!params.isEmpty())
            {
                request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            }
            
            if (body != null)
            {
            	StringEntity se = new StringEntity(body, HTTP.UTF_8);
            	request.setEntity(se);
            }
 
            return executeRequest(request, url);
        }
        }
        return null;
    }
 
    private String executeRequest(HttpUriRequest request, String url) throws Exception
    {
    	HttpParams httpParams = new BasicHttpParams();
    	HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
    	HttpConnectionParams.setSoTimeout(httpParams, 20000);
    	
        HttpClient client = new DefaultHttpClient(httpParams);
 
        HttpResponse httpResponse;
 
        try
        {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();
 
            HttpEntity entity = httpResponse.getEntity();
 
            if (entity != null)
            {
 
                InputStream instream = entity.getContent();
                response = convertStreamToString(instream);
 
                // Closing the input stream will trigger connection release
                instream.close();
                return response;

            }
 
        }
        /*
        catch (ClientProtocolException e)
        {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }*/
        catch (Exception e)
        {
        	client.getConnectionManager().shutdown();
        	throw e;
        }
        return null;
    }
 
    private static String convertStreamToString(InputStream is)
    {
 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}