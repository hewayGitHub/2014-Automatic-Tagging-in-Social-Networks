Automatic-Tagging-in-Social-Networks
====================================

Automatic tagging users in social networks according to their interests
**Still working on it**

**Data** comes from 
http://snap.stanford.edu/data/twitter7.html
and http://an.kaist.ac.kr/traces/WWW2010.html
and some other info crawled by twitter4j

**Data Preprocess** based on [Preprocessing the Informal Text for efficient Sentiment Analysis](http://ijettcs.org/Volume1Issue2/IJETTCS-2012-08-14-047.pdf)
*Text Preprocess*
- Remove retweet, links, non-english word, stopwords
- lowercase and steming

*Specific Preprocess*
filter ordinary words in order to recommend tags to user
- According to idf
- [Twitter LDA](https://github.com/minghui/Twitter-LDA)) can learn the word distribution of background topic
- Recommend tag by information gain or other measure

**Main Method**
- tweet lda: only extract tags from users' tweets with standard LDA
- tag lda: etract tags from users' tweets and follows with tag LDA
- refined-tag lda: we try to use more information of users in a user's following list, currently name and description
- similarity-tag lda: We try to model users' reciprocity

Otherwise, since a user will follow popular users and his(her) friends, colleges as well, but LDA can only model the top words, so in one hand we use similarity-tag lda, in the other hand we will try to reranking users' tag recommended by the above methods.
