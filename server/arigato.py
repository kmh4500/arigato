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

class MainPage(webapp2.RequestHandler):
  def get(self):
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.write(u'ありがとうございます')


class Init(webapp2.RequestHandler):
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
      self.response.write(json.dumps(err, sort_keys=True, indent=4))
      return

    self.response.write('<div>Message sent.</div>')

    phone_numbers = PhoneNumber.query_phone_number(pn)
    if phone_numbers:
      p = phone_numbers[0]
      p.verification_code = vc
    else:
      p = PhoneNumber(phone_number = pn,
                      verification_code = vc)
    p.put()


class Verify(webapp2.RequestHandler):
  def get(self):
    pn = self.request.get('pn')
    phone_numbers = PhoneNumber.query_phone_number(pn)
    if not phone_numbers:
      self.response.write("The phone number doesn't exist.")
      return

    p = phone_numbers[0]
    vc = self.request.get('vc')

    if not p.verification_code == vc:
      self.response.write('Not verified.')
      return

    pk = self.request.get('pk')
    p.public_key = pk
    p.put()
    self.response.write("Verified.")


class Get(webapp2.RequestHandler):
  def get(self):
    pn = self.request.get('pn')
    phone_numbers = PhoneNumber.query_phone_number(pn)
    if not phone_numbers:
      self.response.write("The phone number doens't exist.")
      return

    p = phone_numbers[0]
    self.response.write('<div>%s</div>' % p.public_key)


application = webapp2.WSGIApplication([
  ('/', MainPage),
  ('/init', Init),
  ('/verify', Verify),
  ('/get', Get),
], debug=True)
