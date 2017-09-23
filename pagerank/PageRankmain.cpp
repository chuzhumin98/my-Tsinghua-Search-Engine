#include <iostream>
#include <fstream>
using namespace std;
const int maxpointnum = 52000;
const int pointsize = 52000; 
const int linesize = 52000;
double alpha = 0.15; //转移概率 
int TN = 25; //迭代次数 
class LinkNode {
public:
	int num; //节点编号 
	LinkNode* pred; //前驱
	LinkNode* succ; //后继
	LinkNode() {
		num = -2;
		pred = NULL;
		succ = NULL;
	} 
	LinkNode(int n): num(n), pred(NULL), succ(NULL) {}
	~LinkNode() {
		delete pred;
		delete succ;
	}
	void setLinkNode(int n) {
		num = n;
		pred = NULL;
		succ = NULL;
	}
};
class LinkList {
public:
	int* index; //记录各元素所在数组中的位置
	LinkNode** ll; //记录邻接表的向量
	int* num; //邻接表的向量size 
	int curlen; //当前记录到的位置0
	double* pagerank; //存储各元素的pagerank值 
	LinkList() {
		curlen = 0; 
		index = new int [maxpointnum+1];
		pagerank = new double [pointsize];
		ll = new LinkNode* [pointsize];
		num = new int [pointsize];
		for (int i = 0; i < pointsize; i++) {
			num[i] = 0;
		} 
		for (int i = 0; i < pointsize; i++) {
			ll[i] = new LinkNode;
		}
		for (int i = 0; i <= maxpointnum; i++) {
			index[i] = -1; //-1表示还没有给编号 
		}
	} 
	void inserthead(int num) { //插入一个头 
		if (num < 0 || num > maxpointnum) {
			cout<<"Error need: "<<num<<endl;
			return;
		}
		if (index[num] == -1) { //判断的确需要插
			ll[curlen]->num = num;
			index[num] = curlen;
			curlen++;  
		} 
	}
	void inserttail(int head, int tail) { //插入一个节点 
		int indextmp = index[head];
		if (indextmp == -1) {
			inserthead(head);
			indextmp = index[head];
		}
//		cout<<"prednumtmp:"<<index[tail]<<endl;
		inserthead(tail);
		int numtmp = index[tail];
//		cout<<tail<<" indextmp: "<<indextmp<<" numtmp: "<<numtmp<<endl;		
		LinkNode* ltmp = new LinkNode(numtmp);
		if (ll[indextmp]->succ) {
			ltmp->succ = ll[indextmp]->succ;
			ll[indextmp]->succ->pred = ltmp;
		} 
		ltmp->pred = ll[indextmp];
		ll[indextmp]->succ = ltmp;
	}
	void refreshnum() { //刷新num数组 
		for (int i = 0; i < curlen; i++) {
			int count = 0;
			LinkNode* ltmp = ll[i]->succ;
			while (ltmp) {
				count++;
				ltmp = ltmp->succ;
			}
			num[i] = count;
		} 
	}
	void PageRank() { //实现pagerank算法 
		//初始化 
		for (int i = 0; i < curlen; i++) {
			pagerank[i] = 1.0/curlen;
		} 
		double* pagetmp = new double [curlen];
		for (int i = 0; i < TN; i++) {
			double rcount = 0.0; //记录无后继节点的pagerank值总和
			for (int j = 0; j < curlen; j++) {
				pagetmp[j] = 0.0;
			} 
			for (int j = 0; j < curlen; j++) {
				if (num[j] > 0) { //有后继 
					LinkNode* ltmp = ll[j]->succ;
					while (ltmp) {
						pagetmp[ltmp->num] += (1-alpha)*1.0*pagerank[j]/num[j];
						ltmp = ltmp->succ;
					} 
				} else {
					rcount += (1-alpha)*1.0*pagerank[j];
				}
			}
			for (int j = 0; j < curlen; j++) {
				pagetmp[j] += 1.0*rcount/curlen + 1.0*alpha/curlen; 
			}
			for (int j = 0; j < curlen; j++) {
				pagerank[j] = pagetmp[j];
			}
			double dcount = 0.0; //记录总概率值
			for (int j = 0; j < curlen; j++) {
				dcount += pagerank[j];
			} 
			cout<<"第"<<i+1<<"轮结果总概率和："<<dcount<<endl;
		} 
	} 
	void display(int n) { //display index为n的数组元素 
		cout<<ll[n]->num;
		LinkNode* ltmp = ll[n]->succ;
		while (ltmp) {
			cout<<"->"<<ll[ltmp->num]->num;
			ltmp = ltmp->succ;
		} 
		cout<<endl;
	}
};
int get_Int(char* buffer, int &sign, bool &isend) {
	while (sign[buffer] < '0' || '9' < sign[buffer]) {
		if (buffer[sign] == '\0') {
			isend = true;
			return 0;
		}
		++sign;
	} 
	isend = false;
	int re = 0;
	while ('0' <= sign[buffer] && sign[buffer] <= '9') {
		re = re * 10 + (buffer[sign++] - '0');
	}
	return re;
}
int main() {
	ifstream fin("output.txt");
	ofstream fout("pagerank.xml");
	LinkList l1;
	char* buffer = new char [100000]; //缓存一行字符的数组 
	int linemax = 0;
	int linetmp = 0;
	int count = 0;
	//建立邻接表 
	for (int i = 0; i < linesize; i++) {
		linetmp = 0;
		fin.getline(buffer, 100000);
		int head = -1;
		int tail = 0;
		bool isend = false; //是否结束  
		while (true) {
			if (head == -1) { //头元素未被记录 
				//insert head
				head = get_Int(buffer, linetmp, isend);
				l1.inserthead(head); 
			} else {
				tail = get_Int(buffer, linetmp, isend);
				if (!isend) {
					//insert tail
					l1.inserttail(head, tail);
				} else {
					break;
				}
			}
			count++;
		}
	}
	l1.refreshnum();
	l1.PageRank();
	cout<<"l1.curlen:"<<l1.curlen<<endl; 
	int maxsign = 0;
	for (int i = 0; i < l1.curlen; i++) {
		if (l1.pagerank[i] > l1.pagerank[maxsign]) {
			maxsign = i;
		}
	} 
	cout<<l1.ll[maxsign]->num<<" rank:"<<l1.pagerank[l1.index[l1.ll[maxsign]->num]]<<endl;
	for (int i = 0; i < 50; i++) {
		cout<<"index:"<<i<<" num:"<<l1.ll[i]->num<<" pagerank:"<<l1.pagerank[i]<<endl;
	}
	fout<<"<?xml version=\"1.0\" encoding=\"utf-8\"?>"<<endl;
	fout<<"<docs>"<<endl;
	fout<<"	<category name=\"tsinghua\">"<<endl;
	for (int i = 0; i < l1.curlen; i++) {
		fout<<"<doc id=\""<<l1.ll[i]->num<<"\" pagerank=\""<<l1.pagerank[i]<<"\" />"<<endl;
	}
	fout<<"	</category>"<<endl;
	fout<<"</docs>"<<endl;
	cout<<"Completed"<<endl;
	fout.close();
	fin.close();
	return 0;
} 
