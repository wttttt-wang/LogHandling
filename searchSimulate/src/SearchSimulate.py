#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2017/5/24 20:31
# @Author  : wttttt
# @Github  : https://github.com/wttttt-wang/leetcode
# @Site    : 
# @File    : SearchSimulate.py
# @Software: PyCharm
# @Desc:

import httplib
import time


def get_search_word(file_name):
    # id, question1, question2
    with open(file_name) as fi:
        cnt = 0
        for line in fi:
            splited = line.split(",\"")
            if len(splited) < 3:
                continue
            if len(splited) > 3:
                cnt += 1
                print "Got " + str(cnt) + " irregular line."
		print line
                continue
            conn = httplib.HTTPConnection('10.3.242.99')
            conn.request('GET', 'result.html?input=' + splited[1])
            # time.sleep(5)
            conn = httplib.HTTPConnection('10.3.242.99')
            conn.request('GET', 'result.html?input=' + splited[2])
            time.sleep(0.0005)


def search():
    conn = httplib.HTTPConnection('10.3.242.99')
    word = 'test'
    conn.request('GET', 'result.html?input=' + word)

get_search_word("test.csv")
