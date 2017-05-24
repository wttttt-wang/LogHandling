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
    # id, qid1, qid2, question1, question2, is_duplicate
    with open(file_name) as fi:
        for line in fi:
            splited = line.split(",")
            if len(splited) < 6:
                continue
            conn = httplib.HTTPConnection('10.3.242.101')
            conn.request('GET', 'result.html?input=' + splited[3])
            # time.sleep(5)
            conn = httplib.HTTPConnection('10.3.242.101')
            conn.request('GET', 'result.html?input=' + splited[4])
            time.sleep(5)


def search():
    conn = httplib.HTTPConnection('10.3.242.101')
    word = 'test'
    conn.request('GET', 'result.html?input=' + word)
