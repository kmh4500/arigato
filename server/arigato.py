# coding=utf-8

from google.appengine.ext import ndb

import cgi
import json
import random
import urllib
import webapp2


class PhoneNumber(ndb.Model):
  phone_number = ndb.StringProperty(indexed=True)
  public_key = ndb.StringProperty(indexed=False)
  verification_code = ndb.StringProperty(indexed=False)
  date = ndb.DateTimeProperty(auto_now_add=True)

  @classmethod
  def query_phone_number(cls, phone_number_key):
    return cls.query(PhoneNumber.phone_number==phone_number_key).fetch(1)


class ArigatoResponse(object):
  def __init__(self):
    self.err = 0

  def dump(self):
    return json.dumps(self.__dict__, sort_keys=True)


ArigatoResponse.error_messages = {
  0: 'Success',
  1: 'Fail to send SMS',
  2: 'Fail to find the phone number',
  3: 'Fail to verify',
}


class ArigatoRequestHandler(webapp2.RequestHandler):
  def error(self, err):
    ar = ArigatoResponse()
    ar.err = err
    if self.request.get_all('msg'):
      ar.err_message = ArigatoResponse.error_messages[err]
    self.response.write(ar.dump())

  def success(self):
    ar = ArigatoResponse()
    self.response.write(ar.dump())


class MainPage(webapp2.RequestHandler):
  def get(self):
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.write(u'ありがとうございます')


class Init(ArigatoRequestHandler):
  def get(self):
    # Phone number
    pn = self.request.get('pn')
    if not pn:
      return

    vc = '%6d' % random.randint(0, 999999)
    params = urllib.urlencode({
        'api_key': '982d80f5',
        'api_secret': '96be22a7',
        'from': 'NEXMO',
        'to': pn,
        'text': vc})
    f = urllib.urlopen('https://rest.nexmo.com/sms/json', params)
    err = json.loads(f.read())

    if err['messages'][0]['status'] != '0':
      self.error(1)
      return

    phone_numbers = PhoneNumber.query_phone_number(pn)
    if phone_numbers:
      p = phone_numbers[0]
      p.verification_code = vc
    else:
      p = PhoneNumber(phone_number = pn,
                      verification_code = vc)
    p.put()
    self.success()


class Verify(ArigatoRequestHandler):
  def get(self):
    pn = self.request.get('pn')
    phone_numbers = PhoneNumber.query_phone_number(pn)
    if not phone_numbers:
      self.error(2)
      return

    p = phone_numbers[0]
    vc = self.request.get('vc')

    if not p.verification_code == vc:
      self.error(3)
      return

    pk = self.request.get('pk')
    p.public_key = pk
    p.put()
    self.success()


class Get(ArigatoRequestHandler):
  def get(self):
    pn = self.request.get('pn')
    phone_numbers = PhoneNumber.query_phone_number(pn)
    if not phone_numbers:
      self.error(2)
      return

    p = phone_numbers[0]
    self.success()


application = webapp2.WSGIApplication([
  ('/', MainPage),
  ('/init', Init),
  ('/verify', Verify),
  ('/get', Get),
], debug=True)
