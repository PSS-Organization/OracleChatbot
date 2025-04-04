import unittest
from selenium import webdriver
from selenium.webdriver.common.by import By
from webdriver_manager.firefox import GeckoDriverManager
from selenium.webdriver.firefox.service import Service as FirefoxService
from selenium.webdriver.firefox.options import Options
import logging

class GoogleTestCase(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        logging.basicConfig(level=logging.DEBUG)

    def setUp(self):
        options = Options()
        options.headless = True
        try:
            self.browser = webdriver.Firefox(service=FirefoxService(GeckoDriverManager().install()), options=options)
            logging.info("Firefox WebDriver started successfully.")
        except Exception as e:
            logging.error(f"Failed to start Firefox WebDriver: {e}")
            raise
        self.addCleanup(self.browser.quit)

    def test_page_title(self):
        try:
            self.browser.get('http://www.google.com')
            self.assertIn('Google', self.browser.title)
            logging.info("Page title test passed.")
        except Exception as e:
            logging.error(f"Page title test failed: {e}")
            raise

    def test_img(self):
        try:
            self.browser.get('http://www.google.com')
            someVar = self.browser.find_element(By.XPATH, "/html/body/div[1]/div[2]/div/img")
            logging.info(f"Image found: {someVar}")
        except Exception as e:
            logging.error(f"Image test failed: {e}")
            raise

if __name__ == '__main__':
    unittest.main(verbosity=2)